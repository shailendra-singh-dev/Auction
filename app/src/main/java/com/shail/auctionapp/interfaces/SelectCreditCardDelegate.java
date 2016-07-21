package com.shail.auctionapp.interfaces;

import com.shail.auctionapp.models.CreditCard;
import com.shail.auctionapp.ui.creditcards.CreditCardsFragment;

public interface SelectCreditCardDelegate
{
    public void cardSelected(CreditCardsFragment fragment, CreditCard card);
}
