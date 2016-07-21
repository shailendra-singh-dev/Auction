package com.shail.auctionapp.messaging;

import java.util.HashMap;

import com.shail.auctionapp.messaging.PubNub.PubNubMessageManager;

public class MessageFactory
{
    private static PubNubMessageManager pubNubManager = null;

    public static MessageManager createMessageManager(String providerName, HashMap<String, String> config)
    {
        switch(providerName)
        {
            case "pubnub":
                if (pubNubManager == null)
                {
                    pubNubManager = new PubNubMessageManager(config);
                }

                return pubNubManager;
        }

        return null;
    }
}
