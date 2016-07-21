package com.shail.auctionapp.ui.shippinginfo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.app..R;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.apiclients.UserProfileClient;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.interfaces.ShippingInfoDelegate;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.models.UserProfile;
import com.shail.auctionapp.ui.common.BaseFragment;

public class ShippingInfoFragment extends BaseFragment {
    private static final String TAG = ShippingInfoFragment.class.getSimpleName();

    private EditText editFirstName;
    private EditText editLastName;
    private EditText editPhone;
    private EditText editEmail;

    private EditText editAddress1;
    private EditText editAddress2;
    private EditText editCity;
    private EditText editState;
    private EditText editZip;

    private boolean changed;

    public ShippingInfoDelegate delegate;

    @Override
    protected String getScreenName()
    {
        return SCREEN.SHIPPING.getScreenName();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTopBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_shipping_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        final LinearLayout shippingInfoParent =(LinearLayout) view.findViewById(R.id.shipping_contact_parent);

        editFirstName = (EditText) shippingInfoParent.findViewById(R.id.ship_first_name);
        editLastName = (EditText) shippingInfoParent.findViewById(R.id.ship_last_name);

        editPhone = (EditText) view.findViewById(R.id.ship_phone);
        editEmail = (EditText) view.findViewById(R.id.ship_email);

        editAddress1 = (EditText) view.findViewById(R.id.ship_address1);
        editAddress2 = (EditText) view.findViewById(R.id.ship_address2);

        final LinearLayout shippingAddressParent =(LinearLayout) view.findViewById(R.id.ship_address_parent);

        editCity = (EditText) shippingAddressParent.findViewById(R.id.ship_city);
        editState = (EditText) shippingAddressParent.findViewById(R.id.ship_state);
        editZip = (EditText) shippingAddressParent.findViewById(R.id.ship_zip);

        setupView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_shipping_info, menu);
    }

    public void setup(EditText e) {
        e.addTextChangedListener(
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    changed = true;
                }
            }
        );

        e.setOnEditorActionListener(
            new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        updateProfile();
                        return true;
                    }

                    return false;
                }
            }
        );
    }

    public String value(EditText e)
    {
        return e.getText().toString();
    }

    public void setupView()
    {
        UserProfile profile = Session.getInstance().userProfile;

        editFirstName.setText(profile.firstName);
        editLastName.setText(profile.lastName);
        editPhone.setText(profile.contactNumber);
        editEmail.setText(profile.email);

        editAddress1.setText(profile.shipAddress1);
        editAddress2.setText(profile.shipAddress2);
        editCity.setText(profile.shipCity);
        editState.setText(profile.shipState);
        editZip.setText(profile.shipZip);

        setup(editFirstName);
        setup(editLastName);
        setup(editPhone);
        setup(editEmail);

        setup(editAddress1);
        setup(editAddress2);
        setup(editCity);
        setup(editState);
        setup(editZip);

        changed = false;
    }

    public void updateProfile()
    {
        UserProfile profile = Session.getInstance().userProfile;

        if (!changed)
        {
            updateCompleted();
            return;
        }

        profile.firstName = value(editFirstName);
        profile.lastName = value(editLastName);
        profile.contactNumber = value(editPhone);
        profile.email = value(editEmail);

        profile.shipAddress1 = value(editAddress1);
        profile.shipAddress2 = value(editAddress2);
        profile.shipCity = value(editCity);
        profile.shipState = value(editState);
        profile.shipZip = value(editZip);

        UserProfileClient.updateUserProfile(profile,
            new CallBlockOne<UserProfile>()
            {
                @Override
                public void onSuccess(UserProfile response)
                {
                    updateCompleted();
                }

                @Override
                public void onFailure(Throwable t)
                {
                    Alerts.okAlert(getActivity(), "Error", "Shipping Information", null);
                }
            }
        );
    }

    public void updateCompleted()
    {
        if (delegate != null)
            delegate.shippingInfoUpdated();
    }

    @Override
    protected void updateTopBar()
    {
        updateToolBarTitle(getScreenName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_save:
                updateProfile();
            break;
        }

        return super.onOptionsItemSelected(item);
    }
}
