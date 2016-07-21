package com.shail.auctionapp.apiclients;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.HashMap;

import io.app..BuildConfig;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.messaging.MessageFactory;
import com.shail.auctionapp.messaging.MessageManager;
import com.shail.auctionapp.messaging.gcm.PushNotifications;
import com.shail.auctionapp.models.Config;
import com.shail.auctionapp.models.UserLogin;
import com.shail.auctionapp.models.UserProfile;

public class Session
{
    private static String   prefsStoreName = "Preferences";
    private static Session  mInstance = null;

    // App fields
    public boolean      isDevelopment;
    public String       ApiBaseURL;
    public Config       config;
    public String       chargePublishKey;
    public String       pushDeviceToken;

    public Application      appContext;

    public MixpanelAPI      analytics;
    public MessageManager   messageManager;

    private boolean      appIntroDone;

    // User fields
    public String       sessionUserId;
    public String       sessionKey;
    public UserProfile  userProfile;
    public String       userProfileId;

    private Session()
    {
        //No one should instantiate Session
    }

    public synchronized static Session getInstance()
    {
        if (mInstance == null)
        {
            mInstance               = new Session();
            mInstance.config        = new Config();
            mInstance.isDevelopment = BuildConfig.DEBUG;
        }

        return mInstance;
    }

    @Override
    public String toString() {
        return "[APIBaseURL:" + ApiBaseURL + "]";
    }

    // Session Management

    public void saveSession()
    {
        //context is null when going to the background.  Need to better udnerstand application life cycle
        SharedPreferences prefs = appContext.getSharedPreferences(prefsStoreName, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
            editor.putString("apiBaseURL",          ApiBaseURL);
            editor.putString("sessionKey",          sessionKey);
            editor.putString("sessionUserId",       sessionUserId);
            editor.putString("userProfileId",       userProfileId);
            editor.putString("chargePublishKey",    chargePublishKey);
            editor.putBoolean("sessionSaved",       true);
        editor.commit();

        analytics.flush();
    }

    public void recoverSession()
    {
        SharedPreferences prefs = appContext.getSharedPreferences(prefsStoreName, Context.MODE_PRIVATE);

        if (!prefs.getBoolean("sessionSaved", false))
            return;

        String baseURL = prefs.getString("apiBaseURL", null);
        if ((baseURL != null) && (baseURL.length() > 0))
            ApiBaseURL = baseURL;

        sessionKey          = prefs.getString("sessionKey", null);
        sessionUserId       = prefs.getString("sessionUserId", null);
        userProfileId       = prefs.getString("userProfileId", null);
        chargePublishKey    = prefs.getString("chargePublishKey", null);
    }

    // App Intro screen
    public void introDone()
    {
        SharedPreferences prefs = appContext.getSharedPreferences(prefsStoreName, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("appIntroDone", true);
        editor.apply();
    }

    public boolean isAppIntroDone()
    {
        SharedPreferences prefs = appContext.getSharedPreferences(prefsStoreName, Context.MODE_PRIVATE);

        return prefs.getBoolean("appIntroDone", false);
    }

    public boolean isSetup()
    {
        return messageManager != null;
    }

    // Login / Logout session
    /*
        session     - is an authenticated communication session with the server (we have a sessionKey).
        logged in   - is a session where we have a User (and we know who they are).
    */

    public boolean isValidLogin()
    {
        if (sessionUserId == null)
            return false;

        return sessionUserId.length() > 0;
    }

    public void setupSession(UserLogin login, UserProfile profile)
    {
        sessionKey      = login.id;
        sessionUserId   = login.userId;

        userProfile     = profile;
        userProfileId   = profile.id;

        saveSession();

        CreditCardClient.getPublishKey();

        watchForBids();
    }

    public void logout()
    {
        stopWatchingForBids();

        chargePublishKey    = null;
        userProfileId       = null;
        userProfile         = null;
        sessionKey          = null;
        sessionUserId       = null;

        saveSession();

        createUserProfile(); //we always need a user profile
    }


    // Session Services

    public void startSessionServices()
    {
        //Start by getting the user profile established.
        //  - other services will begin after user profile is established.
        setupUserProfile();

        setupMessaging();
    }

    public void setupUserProfile()
    {
        if ((userProfileId == null) || (userProfileId.length() == 0))
        {
            createUserProfile();
        }
        else
        {
            UserProfileClient.fetchUserProfileById(userProfileId,
                new CallBlockOne<UserProfile>()
                {
                    @Override
                    public void onSuccess(UserProfile response)
                    {
                        UserProfile profile = response;

                        if (profile == null)
                        {
                            //If can't find user profile, create new one.
                            createUserProfile();;
                            return;
                        }

                        useUserProfile(profile);
                        userProfileEstablished();
                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        //quiet failure for now (use the userProfile we auto-created)
                    }
                })
            ;
        }
    }

    public void setAnalytic(String name, String value)
    {
        if ((value == null) || (value.length() == 0))
            return;

        analytics.getPeople().set(name, value);
    }

    public void setupAnalytics()
    {
        analytics.identify(userProfileId);
        analytics.getPeople().identify(userProfileId);

        setAnalytic("$email", userProfile.email);
        setAnalytic("$first_name", userProfile.firstName);
        setAnalytic("$last_name", userProfile.lastName);

        analytics.flush();
    }

    public void userProfileEstablished()
    {
        Log.d("startup", "userProfileEstablished()");

        setupAnalytics();

        setupPush();

        watchForBids();
    }


    // Messaging Services

    public void setupMessaging()
    {
        Log.d("start", "setupMessaging()");

        HashMap<String, String> pubNubConfig = new HashMap<String, String>();

        pubNubConfig.put("publishKey",      config.publishKey);
        pubNubConfig.put("subscribeKey",    config.subscribeKey);

        messageManager = MessageFactory.createMessageManager("pubnub", pubNubConfig);
    }

    public void startPush(String token)
    {
        Log.d("startup", "startPush: " + token);

        pushDeviceToken = token;

        messageManager.setupPush(token);

        //bidWatcher.startPush();

        userProfile.pushDeviceToken = token;
        updateUserProfile();

        analytics.getPeople().identify(userProfileId);
        analytics.getPeople().setPushRegistrationId(token);
        analytics.flush();
    }

    public void setupPush()
    {
        PushNotifications.registerForPushNotifications(appContext);
    }

    // User Profile Management
    public void useUserProfile(UserProfile profile)
    {
        userProfile     = profile;
        userProfileId   = profile.id;

        saveSession();
    }

    public void createUserProfile()
    {
        UserProfileClient.createUserProfile(
                new CallBlockOne<UserProfile>() {
                    @Override
                    public void onSuccess(UserProfile profile) {
                        useUserProfile(profile);
                        userProfileEstablished();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        //this should never happen but can if the network is very sporadic
                        useUserProfile(new UserProfile());
                    }
                }
        );
    }

    public void updateUserProfile()
    {
        if ((userProfileId == null) || (userProfileId.length() == 0))
        {
            UserProfileClient.createUserProfile(
                new CallBlockOne<UserProfile>()
                {
                    @Override
                    public void onSuccess(UserProfile profile)
                    {
                        //Use the current profile and only update the id to connect the profile object back to the server
                        userProfile.id = profile.id;

                        //Now we have an Id let's update the profile on the server
                        UserProfileClient.updateUserProfile(userProfile);

                        //should've done this in the startup flow, but now that we have a good userProfileId spin it all up
                        useUserProfile(userProfile);
                        userProfileEstablished();
                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        //we have failed again so continue to use local userProfile until the network comes back.
                    }
                }
            );
        }
        else
            UserProfileClient.updateUserProfile(userProfile);
    }

    // Bid Watching

    public void watchForBids()
    {
        messageManager.subscribeToPushFor(userProfileId);
    }

    public void stopWatchingForBids()
    {
        messageManager.unsubscribeFromPush(userProfileId);
    }
}
