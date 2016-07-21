package com.shail.auctionapp.messaging.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import io.app..BuildConfig;
import com.shail.auctionapp.apiclients.Session;

public class RegistrationIntentService extends IntentService
{
    private static final String TAG = "RegIntentService";

    public RegistrationIntentService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        try {
            // Initially this call goes out to the network to retrieve the token, subsequent calls are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            InstanceID instanceID   = InstanceID.getInstance(this);
            String token            = instanceID.getToken(BuildConfig._PROJECT_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(TAG, "GCM Registration Token: " + token);

            Session.getInstance().startPush(token);

            Intent registrationComplete = new Intent("registered");
            LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
        }
        catch (Exception e)
        {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
    }
}
