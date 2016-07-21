package com.shail.auctionapp.ui.creditcards;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.card.payment.CardIOActivity;
import io.app..R;
import com.shail.auctionapp.apiclients.CreditCardClient;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.AddCreditCardDelegate;
import com.shail.auctionapp.interfaces.AlertBlock;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.models.CreditCard;
import com.shail.auctionapp.models.CreditCardNewCardInfo;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.models.UserProfile;
import com.shail.auctionapp.ui.common.BaseFragment;
import com.shail.auctionapp.utils.AppConst;
import com.shail.auctionapp.views.CreditCardFormatWatcher;

public class CreditCardAddFragment extends BaseFragment implements View.OnClickListener
{
    private static final String TAG             = CreditCardAddFragment.class.getSimpleName();
    private static int          CardScanAcivity = 1;

    private EditText    editEmail;
    private EditText    editCardNumber;
    private EditText    editCardExpires;
    private EditText    editCardCVV;

    public  AddCreditCardDelegate   delegate;

    @Override
    protected String getScreenName()
    {
        return SCREEN.CARDS_ADD.getScreenName();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateTopBar();
    }

    public CreditCardAddFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_credit_card_add, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        final RelativeLayout creditCardViewParent=(RelativeLayout)view.findViewById(R.id.credit_card_view_parent);

        editEmail       = (EditText)creditCardViewParent.findViewById(R.id.credit_card_add_email);
        editCardNumber  = (EditText)creditCardViewParent.findViewById(R.id.credit_card_add_cardnum);
        editCardNumber.addTextChangedListener(new CreditCardFormatWatcher(editCardNumber));

        final LinearLayout cardExpireCVVParent = (LinearLayout)view.findViewById(R.id.credit_card_add_cardexpire_parent);

        editCardExpires = (EditText)cardExpireCVVParent.findViewById(R.id.credit_card_add_cardexpire);
        editCardCVV     = (EditText)cardExpireCVVParent.findViewById(R.id.credit_card_add_cardcvv);
        editCardCVV.setOnEditorActionListener(
            new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        saveCard();
                        return true;
                    }

                    return false;
                }
            }
        );

        final LinearLayout crediCardScanParent= (LinearLayout)view.findViewById(R.id.credit_card_scan_parent);
        crediCardScanParent.setOnClickListener(this);

        setupView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_add_credit_cards, menu);
    }

    public void setupView()
    {
        editEmail.setText(Session.getInstance().userProfile.email);
    }

    @Override
    protected void updateTopBar()
    {
        updateToolBarTitle(getScreenName());
    }

    public boolean valid()
    {
        String value = editEmail.getText().toString();

        if ((value.length() < 7) || !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches())
        {
            Alerts.okAlert(getActivity(), "Invalid Email", "Please check your email.",
                new AlertBlock() {
                    @Override
                    public void onOk() {
                    }
                }
            );

            editEmail.requestFocus();

            return false;
        }

        CreditCardNewCardInfo info = new CreditCardNewCardInfo();

        info.cardNumber     = editCardNumber.getText().toString().replaceAll(" ", "");
        info.cvv            = editCardCVV.getText().toString();

        info.setExpires(editCardExpires.getText().toString());
        editCardExpires.setText(((info.expiresMonth < 10) ? "0" : "") + info.expiresMonth + "/" + info.expiresYear);

        if (!info.isValid())
        {
            String title;

            if (!info.isValidCardNumber()) {
                title = "Invalid Card Number";

                editCardNumber.requestFocus();
            } else
                if (!info.isValidExpiration()) {
                    title = "Invalid Expiration";

                    editCardExpires.requestFocus();
                } else
                    if (!info.isValidCVV()) {
                        title = "Invalid CVV";
                        editCardCVV.requestFocus();
                    } else
                        title = "Invalid Credit Card Information";

            Alerts.okAlert(getActivity(), title, "Please review and try again.",
                new AlertBlock() {
                    @Override
                    public void onOk() {
                    }
                }
            );

            return false;
        }

        return true;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.credit_card_scan_parent:
                scanCard();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != CardScanAcivity)
            return;

        if (data == null)
            return;

        if (!data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT))
            return;

        io.card.payment.CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

        editCardNumber.setText(scanResult.cardNumber);
        editCardExpires.setText(scanResult.expiryMonth + "/" + scanResult.expiryYear);
        editCardCVV.setText(scanResult.cvv);
    }

    public void scanCard()
    {
        Intent scanIntent = new Intent(getActivity(), CardIOActivity.class);

        scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, true);

        startActivityForResult(scanIntent, CardScanAcivity);
    }

    public void saveCard()
    {
        if (!valid())
            return;

        createCardToken();
    }

    public void createCardToken()
    {
        CreditCardNewCardInfo card  = new CreditCardNewCardInfo();
        card.customerChargeId       = Session.getInstance().userProfile.customerChargeId;
        card.email                  = editEmail.getText().toString();
        card.cardNumber             = editCardNumber.getText().toString().replaceAll("\\s", "");
        card.cvv                    = editCardCVV.getText().toString();

        card.setExpires(editCardExpires.getText().toString());

        progressShow(AppConst.PROGRESS_SHOW_MSG_NOW);

        CreditCardClient.createCard(card,
                new CallBlockOne<CreditCard>() {
                    @Override
                    public void onSuccess(CreditCard response)
                    {
                        progressHide();
                        createReusableClientCardTokens(response);
                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        progressHide();
                        Alerts.okAlert(getAttachedActivity(), "Card Setup Error", t.getMessage(), null);
                    }
                }
        );
    }

    public void createReusableClientCardTokens(CreditCard card)
    {
        if ((card.customerId == null) || (card.customerId.length() == 0))
        {
            UserProfile profile = Session.getInstance().userProfile;

            progressShow(AppConst.PROGRESS_SHOW_MSG_NOW);
            CreditCardClient.createCustomerWithCard(profile.email, card,
                new CallBlockOne<CreditCard>()
                {
                    @Override
                    public void onSuccess(CreditCard response)
                    {
                        progressHide();

                        CreditCard  card    = response;
                        Session session     = Session.getInstance();
                        UserProfile profile = session.userProfile;

                        profile.customerChargeId = card.customerId;

                        session.updateUserProfile();

                        if (delegate != null)
                            delegate.cardAdded(card, CreditCardAddFragment.this);
                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        progressHide();

                        Alerts.okAlert(getActivity(), "Add Customer Card Error", t.getMessage(), null);
                    }
                }
            );
        }
        else
        {
            progressShow(AppConst.PROGRESS_SHOW_MSG_NOW);
            CreditCardClient.addCardToCustomer(card,
                new CallBlockOne<CreditCard>()
                {
                    @Override
                    public void onSuccess(CreditCard response)
                    {
                        progressHide();

                        CreditCard card = response;

                        if (delegate != null)
                            delegate.cardAdded(card, CreditCardAddFragment.this);
                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        progressHide();

                        Alerts.okAlert(getActivity(), "Add Card Error", t.getMessage(), null);
                    }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save_card:
                saveCard();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
