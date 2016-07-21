package com.shail.auctionapp.models;

import com.stripe.android.model.Card;

public class CreditCardNewCardInfo
{
    public String   customerChargeId;
    public String   firstName;
    public String   lastName;
    public String   email;
    public String   cardNumber;
    public int      expiresMonth;
    public int      expiresYear;
    public String   cvv;

    public boolean isValid()
    {
        return isValidCard();
    }

    public boolean isValidCard()
    {
        Card card = new Card(cardNumber, expiresMonth, expiresYear, cvv);

        if (!card.validateCVC())
            return false;

        return card.validateCard();
    }

    public boolean isValidCardNumber()
    {
        return new Card(cardNumber, expiresMonth, expiresYear, cvv).validateNumber();
    }

    public boolean isValidExpiration()
    {
        return new Card(cardNumber, expiresMonth, expiresYear, cvv).validateExpiryDate();
    }

    public boolean isValidCVV()
    {
        return new Card(cardNumber, expiresMonth, expiresYear, cvv).validateCVC();
    }

    public void setExpires(String expires)
    {
        expiresMonth    = 0;
        expiresYear     = 0;

        String[] parts = expires.split("/");
        if ((parts == null) || (parts.length != 2))
            return;

        expiresMonth    = Integer.parseInt(parts[0]);
        expiresYear     = Integer.parseInt(parts[1]);

        // If the user enters 16, make it 2016
        if (expiresYear < 100)
            expiresYear += 2000;
    }
}
