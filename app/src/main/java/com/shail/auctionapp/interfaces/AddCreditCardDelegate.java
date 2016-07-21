package com.shail.auctionapp.interfaces;

import android.support.v4.app.Fragment;

import com.shail.auctionapp.models.CreditCard;

public interface AddCreditCardDelegate
{
    public void cardAdded(CreditCard card, Fragment fragment);
}
