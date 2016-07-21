package com.shail.auctionapp.ui.creditcards;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.app..R;
import com.shail.auctionapp.apiclients.CreditCardClient;
import com.shail.auctionapp.interfaces.AddCreditCardDelegate;
import com.shail.auctionapp.interfaces.SelectCreditCardDelegate;
import com.shail.auctionapp.models.CreditCard;
import com.shail.auctionapp.models.CreditCardBrand;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.ui.common.BaseFragment;
import com.shail.auctionapp.utils.AppUtils;
import com.shail.auctionapp.views.OnItemClickListener;
import com.shail.auctionapp.views.RecyclerViewAdapter;
import com.shail.auctionapp.views.RecyclerViewTouchListener;
import com.shail.auctionapp.views.RecycleViewItemTouchListener;

public class CreditCardsFragment extends BaseFragment implements AddCreditCardDelegate
{
    private static final int TIME_TO_AUTOMATICALLY_DISMISS_ITEM = -1;
    private static final String TAG = CreditCardsFragment.class.getSimpleName();

    private TextView mCreditCardsNotificationView;

    private List<CreditCard>            mCreditCardList = null;

    public SelectCreditCardDelegate delegate;
    private RecyclerView recyclerView;

    public CreditCardsFragment()
    {
    }

    @Override
    protected String getScreenName()
    {
        return SCREEN.CARDS.getScreenName();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateTopBar();
        updateCreditCardsScreen();
    }


    @Override
    public void onStart()
    {
        super.onStart();

        mCreditCardList = CreditCardClient.getCardList();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_credit_cards, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCreditCardsNotificationView = (TextView) view.findViewById(R.id.credit_cards_notification);

        recyclerView = (RecyclerView) view.findViewById(R.id.credit_cards_recyclerview);
        init(recyclerView);
    }

    private void init(RecyclerView recyclerView) {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getAttachedActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        final RecyclerViewAdapter recyclerViewAdapter =new RecyclerViewAdapter(recyclerView) {
            @Override
            protected View createView(Context context, ViewGroup viewGroup, int viewType) {
                return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.credit_cards_row, viewGroup, false);
            }

            @Override
            protected void bindView(int position, ViewHolder viewHolder) {
                final CreditCard creditCard =mCreditCardList.get(position);
                if(null != creditCard){
                    viewHolder.itemView.setTag(creditCard);

                    Typeface typeFace = AppUtils.getTypefaceFromFontFamily(viewHolder.itemView.getContext());

                    TextView creditCardsTypeNameView = (TextView) viewHolder.getView(R.id.credit_card_type_name);
                    creditCardsTypeNameView.setTypeface(typeFace);

                    final LinearLayout creditCardTypeInfo = (LinearLayout) viewHolder.getView(R.id.credit_card_type_info);
                    ImageView creditCardTypeImageView = (ImageView) creditCardTypeInfo.findViewById(R.id.credit_card_type_image);

                    TextView creditCardsNumberView = (TextView) viewHolder.getView(R.id.credit_card_number);
                    creditCardsNumberView.setTypeface(typeFace);
                    final String creditCardNumber = creditCard.cardNumberMasked;
                    creditCardsNumberView.setText(creditCardNumber);

                    final CreditCardBrand creditCardBrand = creditCard.cardType;

                    final String creditCardProviderTypeName = creditCardBrand.getProviderTypeName();
                    creditCardsTypeNameView.setText(creditCardProviderTypeName);

                    creditCardTypeImageView.setImageResource(creditCard.cardType.getProviderImageId());
                }
            }

            @Override
            public int getItemCount() {
                return null == mCreditCardList ? 0 : mCreditCardList.size();
            }

            @Override
            public void onDataAvailable() {
                notifyDataSetChanged();
            }

            @Override
            public void onDataRemovedAtIndex(int index) {
                remove(index);
            }

            @Override
            public void onDataAvailableAtIndex(int index) {
                add(index);
            }
        };

        setIOnDataAvailableListener(recyclerViewAdapter);
        recyclerView.setAdapter(recyclerViewAdapter);

        final RecyclerViewTouchListener<RecyclerViewAdapter> touchListener =
                new RecyclerViewTouchListener<>(recyclerViewAdapter,
                        new RecyclerViewTouchListener.DismissCallbacks<RecyclerViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onPendingDismiss(RecyclerViewAdapter recyclerView, int position) {

                            }

                            @Override
                            public void onDismiss(RecyclerViewAdapter view, int position) {
                                //Take Action on dismiss
                            }
                        });

        touchListener.setDismissDelay(TIME_TO_AUTOMATICALLY_DISMISS_ITEM);
        recyclerView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during RecyclerView scrolling,
        // we don't look for swipes.
        recyclerView.setOnScrollListener((RecyclerView.OnScrollListener) touchListener.makeScrollListener());
        recyclerView.addOnItemTouchListener(new RecycleViewItemTouchListener(getAttachedActivity(),
                new OnItemClickListener()
                {
                    @Override
                    public void onItemClick(View view, int position)
                    {
                        final CreditCard creditCard = mCreditCardList.get(position);

                        if (view.getId() == R.id.txt_delete) {
                            touchListener.processPendingDismisses();
                            mCreditCardList.remove(position);
                            recyclerViewAdapter.remove(position);
                            updateCreditCardsScreen();
                            CreditCardClient.removeCard(creditCard);
                        } else if (view.getId() == R.id.txt_undo) {
                            touchListener.undoPendingDismiss();
                        } else {
                            if (delegate != null)
                                delegate.cardSelected(CreditCardsFragment.this, creditCard);
                            else
                            {
                                    UpdateCreditCardFragment fragment = new UpdateCreditCardFragment();
                                    fragment.creditCard = creditCard;

                                    getAttachedActivity().pushFragment(fragment);
                            }
                        }
                    }
                }
            )
        );
    }

    @Override
    protected void updateTopBar()
    {
        Log.i(TAG, "updateTopBar");

        updateToolBarTitle(getScreenName());
    }

    private void updateCreditCardsScreen()
    {
        if(null == mCreditCardList)
        {
            return;
        }

        Log.d(TAG, "updateCreditCardsScreen() mCreditCardList:" + mCreditCardList);

        if(mCreditCardList.isEmpty())
        {
            mCreditCardsNotificationView.setVisibility(View.VISIBLE);
        }
        else
        {
            mCreditCardsNotificationView.setVisibility(View.GONE);

        }
        if(null != getIOnDataAvailableListener()){
            getIOnDataAvailableListener().onDataAvailable();
        }
    }

    public void cardAdded(CreditCard card, Fragment fragment)
    {
        getAttachedActivity().popFragment();
        
        updateCreditCardsScreen();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_credit_cards, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_card:
                addCreditCard();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addCreditCard()
    {
        CreditCardAddFragment fragment  = new CreditCardAddFragment();
        fragment.delegate               = this;

        getAttachedActivity().pushFragment(fragment);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doCleanUpResources();
    }

    private void doCleanUpResources() {
        if(null != mCreditCardList){
            mCreditCardList.clear();
        }
        mCreditCardList = null;
        recyclerView.setAdapter(null);
    }
}
