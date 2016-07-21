package com.shail.auctionapp.ui.more;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.app..BuildConfig;
import io.app..R;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.interfaces.LoginDelegate;
import com.shail.auctionapp.interfaces.ShippingInfoDelegate;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.ui.about.AboutFragment;
import com.shail.auctionapp.ui.common.BaseFragment;
import com.shail.auctionapp.ui.common.Activity;
import com.shail.auctionapp.ui.creditcards.CreditCardsFragment;
import com.shail.auctionapp.ui.login.LoginFragment;
import com.shail.auctionapp.ui.privacypolicy.PrivacyPolicyFragment;
import com.shail.auctionapp.ui.shippinginfo.ShippingInfoFragment;
import com.shail.auctionapp.ui.termsofuse.TermsFragment;
import com.shail.auctionapp.utils.AppUtils;

public class MoreFragment extends BaseFragment implements View.OnClickListener, LoginDelegate, ShippingInfoDelegate
{
    private static final String TAG = MoreFragment.class.getSimpleName();

    private boolean launchCardsAfterLogin;
    private boolean launchShippingInfoAfterLogin;

    public MoreFragment()
    {
    }

    @Override
    protected String getScreenName()
    {
        return SCREEN.MORE.getScreenName();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Typeface typeFace = AppUtils.getTypefaceFromFontFamily(getAttachedActivity());

        TextView about = (TextView) view.findViewById(R.id.more_about);
        about.setTypeface(typeFace);
        about.setOnClickListener(this);

        TextView shippingInfo = (TextView) view.findViewById(R.id.more_shipping_info);
        shippingInfo.setTypeface(typeFace);
        shippingInfo.setOnClickListener(this);

        TextView creditCards = (TextView) view.findViewById(R.id.more_credit_cards);
        creditCards.setTypeface(typeFace);
        creditCards.setOnClickListener(this);

        TextView privacyPolicy = (TextView) view.findViewById(R.id.more_privacy_policy);
        privacyPolicy.setTypeface(typeFace);
        privacyPolicy.setOnClickListener(this);

        TextView termsAndConditions = (TextView) view.findViewById(R.id.more_terms_and_conditions);
        termsAndConditions.setTypeface(typeFace);
        termsAndConditions.setOnClickListener(this);

        TextView login = (TextView) view.findViewById(R.id.more_login);
        login.setTypeface(typeFace);
        login.setOnClickListener(this);

        TextView logout = (TextView)view.findViewById(R.id.more_logout);
        logout.setTypeface(typeFace);
        logout.setOnClickListener(this);

        if (Session.getInstance().isValidLogin())
        {
            login.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.VISIBLE);
        }
        else
        {
            login.setVisibility(View.VISIBLE);
            logout.setVisibility(View.INVISIBLE);
        }

        // App Version
        TextView appVersionView = (TextView) view.findViewById(R.id.more_app_version);
        appVersionView.setTypeface(typeFace);
        final String appVersion = AppUtils.getAppVersionName();
        final String appBuildType = AppUtils.getAppBuildType();
        final StringBuffer stringBuffer =new StringBuffer();
        stringBuffer.append(appVersion);
        if(BuildConfig.DEBUG){
            stringBuffer.append(" ");
            stringBuffer.append(appBuildType);
        }
        appVersionView.setText(stringBuffer.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTopBar();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected void updateTopBar() {
        Log.i(TAG, "updateTopBar");
        updateToolBarTitle(getScreenName());
        // Set Font [UIFont fontWithName:@"Avenir-Medium" size:20.0]
        // Closest font in Android is Nunito (Book 300)
        updateToolBarTitle(getScreenName());
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.more_about:
                getAttachedActivity().pushFragment(new AboutFragment());
                break;

            case R.id.more_shipping_info:
                shippingInfo();
                break;

            case R.id.more_credit_cards:
                creditCards();
                break;

            case R.id.more_privacy_policy:
                getAttachedActivity().pushFragment(new PrivacyPolicyFragment());
                break;

            case R.id.more_terms_and_conditions:
                getAttachedActivity().pushFragment(new TermsFragment());
                break;

            case R.id.more_login:
                login();
                break;

            case R.id.more_logout:
                logout();
                break;
        }
    }

    public void shippingInfo()
    {
        if (!Session.getInstance().isValidLogin())
        {
            launchShippingInfoAfterLogin = true;
            login();
            return;
        }

        ShippingInfoFragment shipping = new ShippingInfoFragment();
        shipping.delegate = this;
        getAttachedActivity().pushFragment(shipping);
    }

    public void shippingInfoUpdated()
    {

        getAttachedActivity().popFragment();
    }

    public void creditCards()
    {
        if (!Session.getInstance().isValidLogin())
        {
            launchCardsAfterLogin = true;
            login();
            return;
        }

        getAttachedActivity().pushFragment(new CreditCardsFragment());
    }

    public void login()
    {
        LoginFragment login = new LoginFragment();
        login.delegate = this;
        getAttachedActivity().pushFragment(login);
    }

    public void LoginSuccess(LoginFragment fragment)
    {
        final Activity Activity = getAttachedActivity();
        Activity.popFragment();    //pop the login screen

        if (launchCardsAfterLogin)
        {
            launchCardsAfterLogin = false;
            creditCards();
            return;
        }

        if (launchShippingInfoAfterLogin)
        {
            launchShippingInfoAfterLogin = false;
            shippingInfo();
            return;
        }

        Activity.popFragment();    //pop the more screen (ourselves)
    }

    public void logout()
    {
        Session.getInstance().logout();;

        getAttachedActivity().popFragment();
    }
}
