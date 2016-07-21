package com.shail.auctionapp.models;

import io.app..R;

public enum CreditCardBrand
{
    VISA        (0, "Visa",             R.drawable.ic_cc_visa),
    AMEX        (1, "American Express", R.drawable.ic_cc_amex),
    MASTERCARD  (2, "Mastercard",       R.drawable.ic_cc_mastercard),
    DISCOVER    (3, "Discover",         R.drawable.ic_cc_discover),
    JCB         (4, "JCB",              R.drawable.ic_cc_jcb),
    DINERSCLUB  (5, "Diners Club" ,     R.drawable.ic_cc_diners),
    UNKNOWN     (6, "Unknown",          R.drawable.ic_cc_unknown);

    private int     providerValue;
    private String  providerName;
    private int     providerImageId;

    private CreditCardBrand(int value, String toString, int imageId)
    {
        providerValue   = value;
        providerName    = toString;
        providerImageId = imageId;
    }

    public int getProviderImageId()
    {
        return providerImageId;
    }

    public int getProviderTypeIndex()
    {
        return providerValue;
    }

    public String getProviderTypeName()
    {
        return providerName;
    }
}
