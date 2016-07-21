package com.shail.auctionapp.messaging.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import io.app..R;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.ui.common.Activity;
import com.shail.auctionapp.ui.splash.StartupActivity;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService
{
    private static final String TAG = "GcmListenerService";

    public GcmListenerService()
    {
    }

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        String message = data.getString("alert"); // message came from server
        if (message == null)
            message = data.getString("mp_message"); // message came from MixPanel

        Log.d(TAG, "Message: " + message);

        postNotification(message);
    }


    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void postNotification(String message)
    {
        Intent intent;

        if(Session.getInstance().isSetup())
            intent= new Intent(this, Activity.class);
        else
            intent= new Intent(this, StartupActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT); // 0 is the request code

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("")
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        ;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
