package com.shail.auctionapp.views;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class CreditCardFormatWatcher implements TextWatcher
{
    private EditText edit;

    private CreditCardFormatWatcher()
    {
    }

    public CreditCardFormatWatcher(EditText field)
    {
        edit = field;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
    }

    @Override
    public void afterTextChanged(Editable s)
    {
        edit.removeTextChangedListener(this);

        //determine new string
        String oldVal   = s.toString();
        int idx         = edit.getSelectionStart();

        String newVal   = convertToCardFormat(oldVal);

        // Calculate insertion point position
        idx = idx + newVal.length() - oldVal.length();
        if (idx < 0)
            idx = 0;

        if ((idx - 1 > 0) && newVal.substring(idx-1, idx).equals(" "))
            idx -= 1;

        if (idx > newVal.length())
            idx = newVal.length();

        edit.setText(newVal);
        edit.setSelection(idx);

        edit.addTextChangedListener(this);
    }

    private boolean isAmex(String cardNum)
    {
        if (cardNum.length() < 1)
            return false;

        return (cardNum.substring(0, 1).equals("3"));
    }

    private String convertToCardFormat(String cardNum)
    {
        /*
            American Express account numbers begin with “37” or “34”
            15 digits long, format: 3xxx xxxxxx xxxxx

            Visa card numbers begin with “4”
            MasterCard numbers begin with “5”
            Discover’s begin with “6”
            16 digits long, format: xxxx xxxx xxxx xxxx
        */
        if (cardNum.length() == 0)
            return "";

        cardNum = cardNum.replaceAll(" ", "");

        if (isAmex(cardNum))
        {
            //truncate larger than max
            if (cardNum.length() > 15)
                cardNum = cardNum.substring(0, 15);

            // 3714 496353 98431
            // 3782 822463 10005
            if (cardNum.length() > 4)
                cardNum = cardNum.substring(0, 4) + " " + cardNum.substring(4);

            if (cardNum.length() > 11)
                cardNum = cardNum.substring(0, 11) + " " + cardNum.substring(11);
        }
        else
        {
            if (cardNum.length() > 16)
                cardNum = cardNum.substring(0, 16);

            int idx = 4;

            while (idx < cardNum.length())
            {
                cardNum = cardNum.substring(0, idx) + " " + cardNum.substring(idx);
                idx += 5;
            }
        }

        return cardNum;
    }
}
