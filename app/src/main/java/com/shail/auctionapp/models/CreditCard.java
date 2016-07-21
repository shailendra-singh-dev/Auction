package com.shail.auctionapp.models;

import com.google.gson.annotations.SerializedName;

public class CreditCard {
    @SerializedName("cardNumberMasked")
    public String cardNumberMasked;

    @SerializedName("cardType")
    public CreditCardBrand cardType;

    @SerializedName("cardToken")
    public String cardToken;

    @SerializedName("customerId")
    public String customerId;

    @SerializedName("cardId")
    public String cardId;


    @Override
    public String toString() {
        return "[CardNumberMasked" + cardNumberMasked + ",CardType:" + cardType + ",cardToken:" + cardToken + ",customerId:" + customerId + ",cardId:" + cardId+"]";
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public CreditCardBrand getCardType() {
        return cardType;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCardToken() {
        return cardToken;
    }

    public String getCardId() {
        return cardId;
    }
}


