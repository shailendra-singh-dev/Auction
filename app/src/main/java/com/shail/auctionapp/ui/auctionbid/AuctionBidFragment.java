package com.shail.auctionapp.ui.auctionbid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import io.app..R;
import com.shail.auctionapp.apiclients.AuctionItemsClient;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.AddCreditCardDelegate;
import com.shail.auctionapp.interfaces.AuctionBidDelegate;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.interfaces.LoginDelegate;
import com.shail.auctionapp.interfaces.MessageBlock;
import com.shail.auctionapp.interfaces.ShippingInfoDelegate;
import com.shail.auctionapp.messaging.Subscription;
import com.shail.auctionapp.models.AuctionItem;
import com.shail.auctionapp.models.CreditCard;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.models.UserProfile;
import com.shail.auctionapp.ui.common.BaseFragment;
import com.shail.auctionapp.ui.common.Activity;
import com.shail.auctionapp.ui.creditcards.CreditCardAddFragment;
import com.shail.auctionapp.ui.login.LoginFragment;
import com.shail.auctionapp.ui.shippinginfo.ShippingInfoFragment;
import com.shail.auctionapp.utils.AppConst;
import com.shail.auctionapp.utils.AppUtils;
import com.shail.auctionapp.views.CommaFormatWatcher;

public class AuctionBidFragment extends BaseFragment implements View.OnClickListener, ShippingInfoDelegate, LoginDelegate, AddCreditCardDelegate
{
    private static final String TAG = AuctionBidFragment.class.getSimpleName();

    private Button      mCurrentBidInfoButton;
    private EditText    mEnterBidEditBox;
    private Button      mSubmitBidButton;

    private Subscription        subscription;

    public AuctionBidDelegate   delegate;
    public AuctionItem          auctionItem;


    public AuctionBidFragment() {
    }

    @Override
    protected String getScreenName()
    {
        return SCREEN.BID.getScreenName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_bid, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        final LinearLayout enterBidBoxParent= (LinearLayout)view.findViewById(R.id.enter_bid_box_partent);
        mEnterBidEditBox =(EditText)enterBidBoxParent.findViewById(R.id.enter_bid_box);
        mEnterBidEditBox.setOnEditorActionListener(
            new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        submitBid();

                        return true;
                    }

                    return false;
                }
            }
        );

        mEnterBidEditBox.addTextChangedListener(new CommaFormatWatcher(mEnterBidEditBox));

        //Set text based on winner or not..
        mCurrentBidInfoButton = (Button) view.findViewById(R.id.current_bid);

        mSubmitBidButton = (Button) view.findViewById(R.id.submit_bid);
        mSubmitBidButton.setOnClickListener(this);

        trackBidStart();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        focusOnBidding();

        subscribe();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateTopBar();
        updateBidStatus();
        updateBidEntry();
    }

    @Override
    public void OnAppResume()
    {
        AuctionItemsClient.fetchAuctionItemById(auctionItem.id,
            new CallBlockOne<AuctionItem>()
            {
                @Override
                public void onSuccess(AuctionItem response)
                {
                    auctionItem = response;

                    updateBidStatus();
                    updateBidEntry();
                }

                @Override
                public void onFailure(Throwable t)
                {
                }
            }
        );
    }

    @Override
    public void onStop()
    {
        super.onStop();

        unsubscribe();
    }

    @Override
    protected void updateTopBar()
    {
        updateToolBarTitle(getScreenName());
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.submit_bid:
                submitBid();
                break;
        }
    }

    public void trackBidStart()
    {
        Session session = Session.getInstance();

        JSONObject props = new JSONObject();
        try {
            props.put("auctionItemId",      auctionItem.id);
            props.put("auctionItemName",    auctionItem.title);
            props.put("userId",             session.userProfileId);
        } catch (org.json.JSONException ex) {
        }

        session.analytics.track("BidStart", props);
    }

    private void focusOnBidding()
    {
        mEnterBidEditBox.requestFocus();

        InputMethodManager imm = (InputMethodManager)getAttachedActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEnterBidEditBox, InputMethodManager.SHOW_IMPLICIT);
    }

    private void updateBidStatus()
    {
        String currentBidStatus = auctionItem.getBidStatusText(getAttachedActivity());
        mCurrentBidInfoButton.setText(currentBidStatus);
    }

    private void updateBidEntry()
    {
        String enterBidStatus = auctionItem.getEnterBidStatusText(getAttachedActivity());
        mEnterBidEditBox.setText("");
        mEnterBidEditBox.append(enterBidStatus);    //puts insertion point at the end
    }

    private void trackBidSuccess()
    {
        Session session = Session.getInstance();

        JSONObject props = new JSONObject();
        try {
            props.put("auctionItemId",      auctionItem.id);
            props.put("auctionItemName",    auctionItem.title);
            props.put("userId",             session.userProfileId);
        } catch (org.json.JSONException ex) {
        }

        session.analytics.track("BidSuccess", props);
    }

    // Bid submission
    public void submitBid()
    {
        if (!hasAllInfo())
            return;

        String topBidAmountStr  = mEnterBidEditBox.getText().toString();
        float bidAmount         = AppUtils.getFloatFromString(topBidAmountStr.replace(",", ""));

        progressShow(AppConst.PROGRESS_SHOW_MSG_NOW);
        AuctionItemsClient.bidOnAuctionItem(auctionItem, bidAmount,
                new CallBlockOne<AuctionItem>() {
                    @Override
                    public void onSuccess(final AuctionItem newAuctionItem) {
                        progressHide();

                        //update local version of the auctionItem
                        auctionItem.topBidAmount = newAuctionItem.topBidAmount;
                        auctionItem.topBidUserProfileId = newAuctionItem.topBidUserProfileId;

                        biddingComplete();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        progressHide();

                        Alerts.okAlert(getAttachedActivity(), t.getMessage(), null);
                    }
                }
        );
    }

    private void biddingComplete()
    {
        trackBidSuccess();

        if (delegate != null)
            delegate.auctionBidComplete(this);
    }

    private boolean hasAllInfo()
    {
        Activity    activity    = getAttachedActivity();
        Session         session     = Session.getInstance();
        UserProfile     profile     = session.userProfile;

        if (!session.isValidLogin())
        {
            login();

            return false;
        }

        if (!profile.shippingFilledIn())
        {
            final String title = activity.getString(R.string.bid_shipping_info_dialog_title);
            final String message = activity.getString(R.string.bid_shipping_info_dialog_message);
            Alerts.okAlert(activity, title, message, null);

            changeShipping();

            return false;
        }

        if (!profile.contactFilledIn())
        {
            final String title = activity.getString(R.string.bid_contact_info_dialog_title);
            final String message = activity.getString(R.string.bid_contact_info_dialog_message);
            Alerts.okAlert(activity, title, message, null);

            changeShipping();

            return false;
        }

        if (!profile.hasSetupCreditCard())
        {
            final String title = activity.getString(R.string.bid_credit_card_info_dialog_title);
            final String message = activity.getString(R.string.bid_credit_card_info_dialog_message);
            Alerts.okAlert(activity, title, message, null);

            addCreditCard();

            return false;
        }

        return true;
    }

    // Shipping & Delegate Methods

    public void changeShipping()
    {
        ShippingInfoFragment shipping   = new ShippingInfoFragment();
        shipping.delegate               = this;

        getAttachedActivity().pushFragment(shipping);
    }

    public void shippingInfoUpdated()
    {
        getAttachedActivity().popFragment();

        submitBid();
    }

    // Login & Delegate Methods

    public void login()
    {
        LoginFragment login = new LoginFragment();
        login.delegate      = this;

        getAttachedActivity().pushFragment(login);
    }

    public void LoginSuccess(LoginFragment fragment)
    {
        getAttachedActivity().popFragment();    //pop the login screen

        submitBid();
    }

    // Credit Card & Delegate Methods

    public void addCreditCard()
    {
        CreditCardAddFragment creditCardAddFragment = new CreditCardAddFragment();
        creditCardAddFragment.delegate              = this;

        getAttachedActivity().pushFragment(creditCardAddFragment);
    }

    @Override
    public void cardAdded(CreditCard card, Fragment fragment)
    {
        getAttachedActivity().popFragment();

        submitBid();
    }

    // Bid Message Management

    public void auctionChange(JSONObject message)
    {
        String type = message.optString("type");

        switch(type)
        {
            case "end":  //auction ended, can't bid, exit
                getAttachedActivity().popFragment();
            break;
        }
    }

    public void itemChange(JSONObject message)
    {
        String type = message.optString("type");
        if (!type.equals("high"))
            return;

        String auctionItemId = message.optString("auctionItemId");
        if (!auctionItemId.equals(auctionItem.id))
            return;

        // This will be a high bid message
        String bidAmount        = message.optString("bidAmount");
        String userProfileId    = message.optString("userProfileId");

        auctionItem.topBidAmount        = Float.parseFloat(bidAmount);
        auctionItem.topBidUserProfileId = userProfileId;

        //decision was made to not update the input field
        updateBidStatus();
    }


    public void subscribe()
    {
        if (subscription != null)
            return;

        subscription = Session.getInstance().messageManager.subscribeTo("bids",
            new MessageBlock()
            {
                @Override
                public void onMessage(JSONObject message)
                {
                    String group = message.optString("group");

                    switch(group)
                    {
                        case "auction": auctionChange(message); break;
                        case "item":    itemChange(message);    break;
                    }
                }
            }
        );
    }

    public void unsubscribe()
    {
        Session.getInstance().messageManager.unsubscribe(subscription);
        subscription = null;
    }
}
