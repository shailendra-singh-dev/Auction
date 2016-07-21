package com.shail.auctionapp.models;


import io.app..R;
import com.shail.auctionapp.application.Application;

public enum SCREEN {

    SPLASH(R.string.title_none),
    AUCTIONS(R.string.screen_auctions),
    AUCTIONS_ITEMS(R.string.screen_auction_items),
    AUCTIONS_ITEM_DETAILS(R.string.screen_auction_item_details),
    MY_BIDS(R.string.screen_my_bids),
    MORE(R.string.screen_more),
    ABOUT(R.string.screen_about),
    PRIVACY(R.string.screen_privacy_policy),
    TERMS(R.string.screen_terms),
    SHIPPING(R.string.screen_shipping),
    CARDS(R.string.screen_cards),
    CARDS_ADD(R.string.screen_cards_add),
    LOGIN(R.string.screen_login),
    REGISTER(R.string.screen_register),
    BID(R.string.screen_bid),PAYMENT(R.string.screen_payment), UPDATE_CARD(R.string.screen_update_card);

    private int mScreenId;

    SCREEN(int id) {
        mScreenId = id;
    }

    public String getScreenName() {
        return Application.getInstance().getString(mScreenId);
    }
}
