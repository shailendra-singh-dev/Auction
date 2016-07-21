package com.shail.auctionapp.messaging;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import com.shail.auctionapp.interfaces.MessageBlock;

public abstract class MessageManager
{
    private ArrayList<Subscription> subscribers;
    private Activity                activity;

    protected MessageManager(HashMap<String, String> config)
    {
        //This constructor is protected because you can't create an instance of this class

        subscribers = new ArrayList<Subscription>();
    }

    public void setActivity(Activity ctx)
    {
        activity = ctx;
    }

    public Subscription subscribeTo(String channelName, MessageBlock block)
    {
        Subscription sub    = new Subscription();
        sub.name            = channelName;
        sub.block           = block;

        subscribers.add(sub);

        return sub;
    }

    public void unsubscribe(Subscription sub)
    {
        subscribers.remove(sub);
    }

    protected boolean hasChannelName(String channelName)
    {
        for (Subscription sub : subscribers)
            if (channelName.equals(sub.name))
                return true;

        return false;
    }

    protected void distributeForChannel(final String channelName, final JSONObject message)
    {
        activity.runOnUiThread(
            new Runnable()
            {
                @Override
                public void run() {
                    for (Subscription sub : subscribers)
                    {
                        if (!channelName.equals(sub.name))
                            continue;

                        sub.block.onMessage(message);
                    }
                }
            }
        );
    }

    // Push Notifications

    public abstract void setupPush(String deviceToken);
    public abstract void subscribeToPushFor(String channelName);
    public abstract void unsubscribeFromPush(String channelName);
}
