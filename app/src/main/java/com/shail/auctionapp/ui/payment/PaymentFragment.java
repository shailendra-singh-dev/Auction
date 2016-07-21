package com.shail.auctionapp.ui.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import io.app..R;
import com.shail.auctionapp.apiclients.CreditCardClient;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.interfaces.PaymentDelegate;
import com.shail.auctionapp.interfaces.SelectCreditCardDelegate;
import com.shail.auctionapp.interfaces.ShippingInfoDelegate;
import com.shail.auctionapp.models.AuctionItem;
import com.shail.auctionapp.models.CreditCard;
import com.shail.auctionapp.models.CreditCardBrand;
import com.shail.auctionapp.models.CreditCardPurchaseResult;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.models.UserProfile;
import com.shail.auctionapp.ui.common.BaseFragment;
import com.shail.auctionapp.ui.creditcards.CreditCardsFragment;
import com.shail.auctionapp.ui.shippinginfo.ShippingInfoFragment;

public class PaymentFragment extends BaseFragment implements View.OnClickListener, ShippingInfoDelegate, SelectCreditCardDelegate
{
    private static final String TAG = PaymentFragment.class.getSimpleName();

    private TextView mShipMeInfoView;
    private TextView mContactMeInfoView;
    private ImageView mPayWithCardTypeImage;
    private TextView mPayWithCardInfo;

    public AuctionItem      auctionItem;
    public PaymentDelegate  delegate;
    public CreditCard       creditCard;

    public PaymentFragment() {
    }

    @Override
    protected String getScreenName() {
        return SCREEN.PAYMENT.getScreenName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View paymentViewParent = inflater.inflate(R.layout.fragment_payment, container, false);

        //Ship Me..
        final RelativeLayout shipMeParent = (RelativeLayout) paymentViewParent.findViewById(R.id.ship_me_parent);

        final TextView shipMeChangeView = (TextView) shipMeParent.findViewById(R.id.ship_me_change);
        shipMeChangeView.setOnClickListener(this);

        mShipMeInfoView = (TextView) paymentViewParent.findViewById(R.id.ship_me_info);

        //Contact me..
        final RelativeLayout contactMeParent = (RelativeLayout) paymentViewParent.findViewById(R.id.contact_me_parent);

        final TextView contactMeChangeView = (TextView) contactMeParent.findViewById(R.id.contact_me_change);
        contactMeChangeView.setOnClickListener(this);

        mContactMeInfoView = (TextView) paymentViewParent.findViewById(R.id.contact_me_info);

        //Pay with
        final RelativeLayout payWithParent = (RelativeLayout) paymentViewParent.findViewById(R.id.pay_with_parent);

        final TextView payWithChangeView = (TextView) payWithParent.findViewById(R.id.pay_with_change);
        payWithChangeView.setOnClickListener(this);

        final LinearLayout payWithCardParent = (LinearLayout) paymentViewParent.findViewById(R.id.pay_with_card_parent);
        mPayWithCardTypeImage = (ImageView) payWithCardParent.findViewById(R.id.pay_with_card_image);

        mPayWithCardInfo = (TextView) paymentViewParent.findViewById(R.id.pay_with_card_info);

        final Button buyButton = (Button) paymentViewParent.findViewById(R.id.buy);
        buyButton.setOnClickListener(this);

        return paymentViewParent;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (creditCard == null)
            creditCard = CreditCardClient.getDefaultCard();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateTopBar();
        setupView();
    }

    private void setupView()
    {
        Session session     = Session.getInstance();
        UserProfile profile = session.userProfile;

        //Update Payment Info...
        //Test values are used..To be removed when all information are available..
        final String name       = profile.firstName + " " + profile.lastName;
        final String address1   = profile.shipAddress1;
        final String address2   = profile.shipAddress2;
        final String street     = profile.shipCity + ", " + profile.shipState + " " + profile.shipZip;
        final String shipInfo   = String.format(getResources().getString(R.string.payment_address), name, address1, address2, street);
        mShipMeInfoView.setText(shipInfo);

        final String number         = profile.contactNumber;
        final String emailId        = profile.email;
        final String contactInfo    = String.format(getResources().getString(R.string.contact_me_info), number, emailId);
        mContactMeInfoView.setText(contactInfo);

        if(null != creditCard){
            final CreditCardBrand creditCardBrand = creditCard.cardType;
            mPayWithCardTypeImage.setImageResource(creditCardBrand.getProviderImageId());
            final String cardType           = creditCardBrand.getProviderTypeName();
            final String cardNumber         = creditCard.cardNumberMasked;
            final String paymentCardInfo    = String.format(getResources().getString(R.string.payment_card_info), cardType, cardNumber);
            mPayWithCardInfo.setText(paymentCardInfo);
        }
    }

    @Override
    protected void updateTopBar() {
        updateToolBarTitle(getScreenName());
    }


    public void trackPaid()
    {
        Session session = Session.getInstance();

        JSONObject props = new JSONObject();
        try {
            props.put("auctionItemId",      auctionItem.id);
            props.put("auctionItemName",    auctionItem.title);
            props.put("userId",             session.userProfileId);
        } catch (org.json.JSONException ex) {
        }

        session.analytics.track("AuctionItemPaid", props);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.ship_me_change:
                changeShipping();
            break;

            case R.id.contact_me_change:
                changeShipping();
            break;

            case R.id.pay_with_change:
                changeCard();
            break;

            case R.id.buy:
                purchaseItem();
            break;

        }
    }

    // Shipping Info change
    public void changeShipping()
    {
        ShippingInfoFragment shipping   = new ShippingInfoFragment();
        shipping.delegate               = this;

        getAttachedActivity().pushFragment(shipping);
    }

    public void shippingInfoUpdated()
    {
        getAttachedActivity().popFragment();
    }

    // Credit Card change

    public void changeCard()
    {
        CreditCardsFragment fragment    = new CreditCardsFragment();
        fragment.delegate               = this;

        getAttachedActivity().pushFragment(fragment);
    }

    public void cardSelected(CreditCardsFragment fragment, CreditCard card)
    {
        creditCard = card;

        getAttachedActivity().popFragment();
    }

    // Pay

    public void purchaseItem()
    {
        // make payment
        CreditCardClient.chargeCard(creditCard, auctionItem,
            new CallBlockOne<CreditCardPurchaseResult>()
            {
                @Override
                public void onSuccess(CreditCardPurchaseResult response)
                {
                    trackPaid();

                    if (delegate != null)
                        delegate.paymentSuccess(PaymentFragment.this);
                }

                @Override
                public void onFailure(Throwable t)
                {
                    Alerts.okAlert(getAttachedActivity(), "Purchase Error", t.getLocalizedMessage(), null);
                }
            }
        );
    }
}
