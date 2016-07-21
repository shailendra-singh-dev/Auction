package com.shail.auctionapp.messaging.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PushNotifications
{
    private PushNotifications()
    {
    }

    public static void registerForPushNotifications(Context context)
    {
        BroadcastReceiver receiver =
            new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    Log.d("push", "startup message");
                }
            }
        ;

        LocalBroadcastManager.getInstance(null).registerReceiver(receiver, new IntentFilter("registered"));

        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(context, RegistrationIntentService.class);
        context.startService(intent);
    }
}
