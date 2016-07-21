package com.shail.auctionapp.views;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;

import com.shail.auctionapp.utils.AppUtils;

public class CommaFormatWatcher implements TextWatcher
{
    private EditText        edit;
    private DecimalFormat   formatter;

    private CommaFormatWatcher()
    {
    }

    public CommaFormatWatcher(EditText field)
    {
        edit        = field;
        formatter   = new DecimalFormat("#,###.00");

        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(2);
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

        float bidAmount = AppUtils.getFloatFromString(oldVal.replace(",", ""));
        String newVal   = (bidAmount == 0.0f) ? "" : formatter.format(bidAmount);

        // add fractional parts when it doesn't format to the proper display number.
        if ((oldVal.length() > 0) && (oldVal.substring(oldVal.length() - 1).equals(".")))
            newVal = newVal + ".";

        if ((oldVal.length() > 1) && (oldVal.substring(oldVal.length() - 2).equals(".0")))
            newVal = newVal + ".0";

        // Calculate insertion point position
        idx = idx + newVal.length() - oldVal.length();
        if (idx < 0)
            idx = 0;
        else
            if ((idx > 0) && (newVal.substring(idx - 1, idx).equals(",")))
                idx -= 1;

        edit.setText(newVal);
        edit.setSelection(idx);

        edit.addTextChangedListener(this);
    }
}
