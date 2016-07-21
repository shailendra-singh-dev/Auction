package com.shail.auctionapp.messaging.PubNub;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONObject;

import java.util.HashMap;

import com.shail.auctionapp.interfaces.MessageBlock;
import com.shail.auctionapp.messaging.MessageManager;
import com.shail.auctionapp.messaging.Subscription;

public class PubNubMessageManager extends MessageManager
{
    public Pubnub   client;
    public String   deviceToken;

    public PubNubMessageManager(HashMap<String, String> config)
    {
        super(config);

        String publishKey   = config.get("publishKey");
        String subscribeKey = config.get("subscribeKey");

        Log.d("pubnub", "new Pubnub()");
        client = new Pubnub(publishKey, subscribeKey);
    }

    public Subscription subscribeTo(String channelName, MessageBlock block)
    {
        boolean subscribed  = hasChannelName(channelName);
        Subscription sub    = super.subscribeTo(channelName, block);

        if (!subscribed)
        {
            try
            {
                client.subscribe(channelName,
                    new Callback()
                    {
                        @Override
                        public void successCallback(String channel, Object message)
                        {
                            super.successCallback(channel, message);

                            //only allow JSON/dictionary messages
                            if (!(message instanceof JSONObject))
                                return;

                            distributeForChannel(channel, (JSONObject)message);
                        }
                    }
                );
            }
            catch (PubnubException e)
            {
                e.printStackTrace();
            }
    }

        return sub;
    }


    public void unsubscribe(Subscription sub)
    {
        if (sub == null)    //user might try to escape before initial subscription has been set.
            return;

        super.unsubscribe(sub);

        if (!hasChannelName(sub.name))
            client.unsubscribe(sub.name);
    }

    // Push Notifications

    public void setupPush(String token)
    {
        deviceToken = token;
    }

    public void subscribeToPushFor(String channelName)
    {
        client.enablePushNotificationsOnChannel(channelName, deviceToken);
    }

    public void unsubscribeFromPush(String channelName)
    {
        client.disablePushNotificationsOnChannel(channelName, deviceToken);
    }
}
