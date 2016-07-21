package com.shail.auctionapp.models;


import com.shail.auctionapp.apiclients.CreditCardClient;

public class UserProfile
{
    public String id;
    public String userId;
    public String customerChargeId;
    public String pushDeviceToken;
    public String firstName;
    public String lastName;
    public String email;
    public String contactNumber;
    public String shipAddress1;
    public String shipAddress2;
    public String shipCity;
    public String shipState;
    public String shipZip;

    public boolean hasSetupCreditCard()
    {
        if ((customerChargeId == null) || (customerChargeId.length() == 0))
            return false;

        if (CreditCardClient.getCardList().size() == 0)
            return false;

        return true;
    }

    public boolean contactFilledIn()
    {
        if ((firstName == null) || (firstName.length() == 0))
            return false;

        if ((lastName == null) || (lastName.length() == 0))
            return false;

        if ((contactNumber == null) || (contactNumber.length() == 0))
            return false;

        if ((email == null) || (email.length() == 0))
            return false;

        return true;
    }

    public boolean shippingFilledIn()
    {
        if ((shipAddress1 == null) || (shipAddress1.length() == 0))
            return false;

        if ((shipCity == null) || (shipCity.length() == 0))
            return false;

        if ((shipState == null) || (shipState.length() == 0))
            return false;

        if ((shipZip == null) || (shipZip.length() == 0))
            return false;

        return true;
    }
}
