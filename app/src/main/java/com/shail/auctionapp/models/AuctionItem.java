package com.shail.auctionapp.models;

import android.content.Context;

import java.util.List;

import io.app..R;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.utils.AppUtils;

public class AuctionItem
{
    public enum AUCTION_ITEM_STATE
    {
        PENDING("pending"), OPEN("open"), PULLED("pulled"), CLOSED("closed"), PAID("paid");

        private String mState;

        AUCTION_ITEM_STATE(final String state) {
            mState = state;
        }

        public String getState() {
            return mState;
        }
    }

    public String id;
    public String auctionId;
    public String itemState;

    public String title;
    public String content;
    public float minimumBid;
    public float increment;

    public float topBidAmount;
    public String topBidUserProfileId;
    public String winnerMessage;

    public List<Asset> assets;

    @Override
    public String toString() {
        return "[id:" + id + ",itemState:" + itemState + ",title:" + title + ",topBidAmount:" + topBidAmount + "]";
    }

    @Override
    public boolean equals(final Object object)
    {
        if ((object == null) || !(object instanceof AuctionItem) || (id == null) || (id.isEmpty()))
            return false;

        final AuctionItem auctionItem = (AuctionItem) object;
        return id.equalsIgnoreCase(auctionItem.id);
    }


    public boolean topBidder()
    {
        if (null == topBidUserProfileId || topBidUserProfileId.isEmpty()) {
            return false;
        }

        final String userProfileId = Session.getInstance().userProfileId;
        if (null == userProfileId || userProfileId.isEmpty()) {
            return false;
        }
        return userProfileId.equalsIgnoreCase(topBidUserProfileId);
    }

    public boolean isPending() {
        if (null == itemState || itemState.isEmpty()) {
            return false;
        }
        return itemState.equalsIgnoreCase(AUCTION_ITEM_STATE.PENDING.getState());
    }

    public boolean isAvailable() {
        if (null == itemState || itemState.isEmpty()) {
            return false;
        }
        return itemState.equalsIgnoreCase(AUCTION_ITEM_STATE.OPEN.getState());
    }

    public boolean isPulled() {
        if (null == itemState || itemState.isEmpty()) {
            return false;
        }
        return itemState.equalsIgnoreCase(AUCTION_ITEM_STATE.PULLED.getState());
    }

    public boolean isClosed() {
        if (null == itemState || itemState.isEmpty()) {
            return false;
        }
        return itemState.equalsIgnoreCase(AUCTION_ITEM_STATE.CLOSED.getState());
    }

    public void hasPaid()
    {
        itemState = AUCTION_ITEM_STATE.PAID.getState();
    }

    public void setItemStateClosed()
    {
        itemState = AUCTION_ITEM_STATE.CLOSED.getState();
    }

    public boolean isPaid() {
        if (null == itemState || itemState.isEmpty()) {
            return false;
        }
        return itemState.equalsIgnoreCase(AUCTION_ITEM_STATE.PAID.getState());
    }

    public void setItemState(final AUCTION_ITEM_STATE auction_item_state) {
        itemState = auction_item_state.getState();
    }

    public String getMyBidStatusText(final Context context) {
        int messageId = 0;
        if (isPaid())
            messageId = topBidder() ? R.string.my_bids_topbidder_won : R.string.my_bids_auction_closed;

        if (isPulled())
            messageId = R.string.my_bids_closed;

        if (isClosed())
            messageId = topBidder() ? R.string.my_bids_won : R.string.my_bids_auction_closed;

        if (!topBidder())
            messageId = R.string.my_bids_topbidder_outbid;
        else
            messageId = R.string.my_bids_topbidder;

        return context.getString(messageId);
    }

    public String getBidStatusText(final Context context)
    {
        Session session = Session.getInstance();
        int messageId = 0;

        if (isPending())
            return context.getString(R.string.bid_pending);

        if (isPaid())
        {
            if ((topBidUserProfileId != null) && (session.userProfileId != null) && topBidUserProfileId.equalsIgnoreCase(session.userProfileId))
                return context.getString(R.string.bid_purchased);
            else {
                if (topBidAmount == 0.0f)
                    return context.getString(R.string.bid_closed);
                else
                    return AppUtils.getFloatFormatFromStringId(context, R.string.closing_bid_amount, topBidAmount);
            }
        }

        if (isPulled())
            return context.getString(R.string.bid_closed);

        if (topBidder()) {
            if (isAvailable())
                return AppUtils.getFloatFormatFromStringId(context, R.string.top_bid_amount, topBidAmount);
            else
                return AppUtils.getFloatFormatFromStringId(context, R.string.top_bid_won, topBidAmount);
        } else {
            if (topBidAmount == 0.0f)
                messageId = R.string.no_bids;
            else {
                if (isAvailable())
                    return AppUtils.getFloatFormatFromStringId(context, R.string.current_bid_amount, topBidAmount);
                else
                    return AppUtils.getFloatFormatFromStringId(context, R.string.closing_bid_amount, topBidAmount);
            }
        }

        return context.getString(messageId);
    }

    public String bidButtonText()
    {
        if (isPaid())
            return (topBidder()) ? "Purchased" : "Bidding Closed";

        if (isPulled())
            return "Bidding Closed";

        if (isClosed())
            return (topBidder()) ? "Tap to Pay" : "Bidding Closed";

        return "Place Bid";
    }

    public String getEnterBidStatusText(final Context context)
    {
        return AppUtils.getFloatFormatFromStringId(context, R.string.enter_bid_amount, (topBidAmount < minimumBid) ? minimumBid : topBidAmount + increment);
    }

    public String getWinnerMessage(){
        return winnerMessage;
    }

    public String getTitle(){
        return title;
    }
}
