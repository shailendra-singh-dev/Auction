package com.shail.auctionapp.ui.creditcards;

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
import android.widget.TextView;

import io.app..R;
import com.shail.auctionapp.apiclients.CreditCardClient;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.models.CreditCard;
import com.shail.auctionapp.models.CreditCardNewCardInfo;
import com.shail.auctionapp.models.CreditCardUpdateResult;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.ui.common.BaseFragment;

public class UpdateCreditCardFragment extends BaseFragment
{
    private static final String TAG = UpdateCreditCardFragment.class.getSimpleName();

    private TextView mCreditCardNumberInfoView;
    private EditText mEditCardExpiryView;

    public CreditCard creditCard;

    public UpdateCreditCardFragment()
    {
    }

    @Override
    protected String getScreenName() {
        return SCREEN.UPDATE_CARD.getScreenName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_update_card, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mCreditCardNumberInfoView = (TextView) view.findViewById(R.id.credit_card_number_masked);
        mEditCardExpiryView = (EditText) view.findViewById(R.id.credit_card_expire);
        mEditCardExpiryView.setOnEditorActionListener(
            new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        updateCard();
                        return true;
                    }

                    return false;
                }
            }
        );
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
        String cardNumberMasked = creditCard.getCardNumberMasked();

        mCreditCardNumberInfoView.setText(cardNumberMasked);
    }

    @Override
    protected void updateTopBar() {
        updateToolBarTitle(getScreenName());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_update_credit_cards, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_upadate_card:
                updateCard();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateCard()
    {
        CreditCardNewCardInfo info = new CreditCardNewCardInfo();

        info.setExpires(mEditCardExpiryView.getText().toString());

        if (!info.isValidExpiration())
        {
            Alerts.okAlert(getAttachedActivity(), "Invalid Expiration", "Please check the credit card expiration you entered.", null);
            return;
        }

        CreditCardClient.updateCard(creditCard, info.expiresMonth, info.expiresYear,
            new CallBlockOne<CreditCardUpdateResult>()
            {
                @Override
                public void onSuccess(CreditCardUpdateResult response)
                {
                    // probably should implement a delegate here
                    getAttachedActivity().popFragment();
                }

                @Override
                public void onFailure(Throwable t)
                {
                    Alerts.okAlert(getAttachedActivity(), "Card Update Error", t.getMessage(), null);
                }
            }
        );
    }
}
