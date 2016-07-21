package com.shail.auctionapp.models;

import java.util.List;

public class Auction
{
    public enum AUCTION_STATE
    {
        OPEN("open"),
        CLOSED("closed");

        private String mState;

        AUCTION_STATE(final String state) {
            mState = state;
        }

        public String getState() {
            return mState;
        }

    }

    public String id;
    public String auctionState;
    public String title;
    public String content;
    public String sponsoredBy;

    public List<Asset> assets;

    @Override
    public String toString() {
        return "[ID:" + id + ",Title:" + title + ",auctionState:" + auctionState + "]";
    }

    public boolean isOpen() {
        if (null == auctionState || auctionState.isEmpty()) {
            return false;
        }
        return auctionState.equalsIgnoreCase(AUCTION_STATE.OPEN.getState());
    }

    public void hasOpened()
    {
        auctionState = "open";
    }

    public void hasClosed()
    {
        auctionState = "closed";
    }

    @Override
    public boolean equals(final Object object)
    {
        if ((object == null) || !(object instanceof Auction) || (id == null) || (id.isEmpty())) {
            return false;
        }
        final Auction auction = (Auction) object;
        return id.equalsIgnoreCase(auction.id);
    }

    public String getSponsoredBy() {
        return sponsoredBy;
    }

}
