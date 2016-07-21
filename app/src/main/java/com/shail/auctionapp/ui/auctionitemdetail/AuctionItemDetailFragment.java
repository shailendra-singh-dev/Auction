package com.shail.auctionapp.ui.auctionitemdetail;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.app..R;
import com.shail.auctionapp.apiclients.AuctionItemsClient;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.AuctionBidDelegate;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.interfaces.MessageBlock;
import com.shail.auctionapp.interfaces.PaymentDelegate;
import com.shail.auctionapp.messaging.Subscription;
import com.shail.auctionapp.models.Asset;
import com.shail.auctionapp.models.Auction;
import com.shail.auctionapp.models.AuctionItem;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.ui.auctionbid.AuctionBidFragment;
import com.shail.auctionapp.ui.common.BaseFragment;
import com.shail.auctionapp.ui.common.Activity;
import com.shail.auctionapp.ui.payment.PaymentFragment;
import com.shail.auctionapp.views.AuctionDetailsImageViewPager;
import com.shail.auctionapp.views.CirclePageIndicator;


public class AuctionItemDetailFragment extends BaseFragment implements ViewPager.OnPageChangeListener, View.OnClickListener, AuctionBidDelegate, PaymentDelegate
{
    private static String TAG = AuctionItemDetailFragment.class.getSimpleName();
    private static int currentPage = 0;

    private AuctionItemDetailImageAdapter   mAuctionItemsImage_Adapter;
    private ArrayList<String>               mImagesArray = new ArrayList<String>();

    private Button      mCurrentBid;
    private TextView    sponsorInfoView;
    private TextView    winnerInfoView;
    private TextView    descriptionInfoView;
    private Button      mPlaceBid;
    private TextView    mAuctionTitleView;

    private Subscription subscription;

    public AuctionItem  auctionItem;
    public Auction      auction;

    public AuctionItemDetailFragment()
    {
    }

    @Override
    protected String getScreenName()
    {
        return SCREEN.AUCTIONS_ITEM_DETAILS.getScreenName();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        mAuctionItemsImage_Adapter = new AuctionItemDetailImageAdapter(mImagesArray);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(null != auctionItem){
            final List<Asset> assets = auctionItem.assets;
            String thumbnailUrl = Session.getInstance().ApiBaseURL + "assets/" + auctionItem.auctionId + "/download/";
            for (final Asset asset : assets) {
                final String url = thumbnailUrl + asset.url;
                mImagesArray.add(url);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view   = inflater.inflate(R.layout.fragment_auction_item_details, container, false);

        // Setup buttons
        mCurrentBid = (Button)view.findViewById(R.id.auction_item_current_bid);

        mPlaceBid   = (Button)view.findViewById(R.id.auction_item_place_bid);
        mPlaceBid.setOnClickListener(this);

        // Setup Content Views
        mAuctionTitleView = (TextView)view.findViewById(R.id.auction_item_title);
        winnerInfoView = (TextView)view.findViewById(R.id.auction_item_winner_info);
        sponsorInfoView = (TextView)view.findViewById(R.id.auction_item_sponsor_info);

        ScrollView descriptionInfoViewParent = (ScrollView)view.findViewById(R.id.auction_item_description_parent);
        descriptionInfoView = (TextView) descriptionInfoViewParent.findViewById(R.id.auction_item_description);

        // Setup Images pager
        setupImages(view);
        setupContent();
        refreshBidInfo();

        return view;
    }

    private void setupImages(View view)
    {
        AuctionDetailsImageViewPager auctionDetailsImageViewPager = (AuctionDetailsImageViewPager)view.findViewById(R.id.pager);

        auctionDetailsImageViewPager.setAdapter(mAuctionItemsImage_Adapter);
        auctionDetailsImageViewPager.setCurrentItem(currentPage, true);

        CirclePageIndicator indicator = (CirclePageIndicator) view.findViewById(R.id.indicator);
        indicator.setViewPager(auctionDetailsImageViewPager);

        final float density = getResources().getDisplayMetrics().density;
        indicator.setRadius(5 * density);

        // Pager listener over indicator
        indicator.setOnPageChangeListener(this);

        mAuctionItemsImage_Adapter.notifyDataSetChanged();
    }

    private void setupContent()
    {
        String auctionTitle = auctionItem.getTitle();
        if ((auctionTitle == null) || auctionTitle.isEmpty())
            auctionTitle = "";

        mAuctionTitleView.setText(auctionTitle);

        String sponsoredBy = "";
        if (auction != null)
        {
            sponsoredBy = auction.getSponsoredBy();
            if ((sponsoredBy == null) || sponsoredBy.isEmpty())
            {
                sponsorInfoView.setVisibility(View.GONE);
            } else {
                sponsorInfoView.setVisibility(View.VISIBLE);
                sponsorInfoView.setText(sponsoredBy);
            }
        }

        String winnerInfo = "";
        if ((auctionItem.isClosed() || auctionItem.isPaid()) && auctionItem.topBidder())
        {
            winnerInfo = auctionItem.getWinnerMessage();
        }

        if ((winnerInfo == null) || winnerInfo.isEmpty())
        {
            winnerInfoView.setVisibility(View.GONE);
        } else {
            winnerInfoView.setVisibility(View.VISIBLE);
            winnerInfoView.setText(winnerInfo);
        }

        String description = auctionItem.content;
        if ((description == null) || description.isEmpty())
        {
            descriptionInfoView.setVisibility(View.GONE);
        } else {
            descriptionInfoView.setVisibility(View.VISIBLE);
            descriptionInfoView.setText(description);
        }
    }

    public void refreshBidInfo()
    {
        Activity activity = getAttachedActivity();

        mCurrentBid.setText(auctionItem.getBidStatusText(activity));

        mPlaceBid.setText(auctionItem.bidButtonText());
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        trackViewAuctionItemDetail();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        subscribe();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateTopBar();
        refreshBidInfo();
    }

    @Override
    public void OnAppResume()
    {
        refreshAuctionItem();
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
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
    }

    @Override
    public void onPageSelected(int position)
    {
        currentPage = position;
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {
    }

    public void trackViewAuctionItemDetail()
    {
        Session session = Session.getInstance();

        JSONObject props = new JSONObject();
        try {
            props.put("auctionItemId",      auctionItem.id);
            props.put("auctionItemName",    auctionItem.title);
            props.put("userId",             session.userProfileId);
        } catch (org.json.JSONException ex) {
        }

        session.analytics.track("ViewAuctionItemDetail", props);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.auction_item_place_bid:
                placeBid();
                break;
        }
    }

    public void placeBid()
    {
        if (auctionItem.isPending())
        {
            Alerts.okAlert(getAttachedActivity(), "Auction Bid", "This item is not yet available for bidding.", null);
            return;
        }

        if (auctionItem.isAvailable())
        {
            if (auctionItem.topBidder())
                Alerts.okAlert(getAttachedActivity(), "Auction Bid", "You are the top bidder.", null);
            else
                captureBid();

            return;
        }

        if (auctionItem.isPaid())
        {
            if (auctionItem.topBidder())
                Alerts.okAlert(getAttachedActivity(), "Congratulations!", "You're the winner.", null);
            else
                Alerts.okAlert(getAttachedActivity(), "Auction Bid", "Bidding is Closed.", null);

            return;
        }

        if (auctionItem.isClosed() || auctionItem.isPulled())
        {
            if (auctionItem.topBidder())
                payForItem();
            else
                Alerts.okAlert(getAttachedActivity(), "Auction Bid", "Bidding is Closed.", null);

            return;
        }
    }

    public void captureBid()
    {
        AuctionBidFragment fragment = new AuctionBidFragment();
        fragment.auctionItem        = auctionItem;
        fragment.delegate           = this;

        getAttachedActivity().pushFragment(fragment);
    }

    // Payment processing

    private void payForItem()
    {
        PaymentFragment fragment    = new PaymentFragment();
        fragment.auctionItem        = auctionItem;
        fragment.delegate           = this;

        getAttachedActivity().pushFragment(fragment);;
    }

    @Override
    public void paymentSuccess(PaymentFragment fragment)
    {
        //this will be updated by the message, but we can update it now
        auctionItem.hasPaid();

        getAttachedActivity().popFragment();
    }

    @Override
    public void auctionBidComplete(AuctionBidFragment fragment)
    {
        getAttachedActivity().popFragment();

        refreshBidInfo();
    }

    public void refreshAuctionItem()
    {
        AuctionItemsClient.fetchAuctionItemById(auctionItem.id,
                new CallBlockOne<AuctionItem>() {
                    @Override
                    public void onSuccess(AuctionItem response) {
                        auctionItem = response;

                        refreshBidInfo();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        //just quietly fail for now
                    }
                }
        );
    }

    // messaging

    public void auctionChange(JSONObject message)
    {
        String type = message.optString("type");

        switch(type)
        {
            case "start": //auction started
                auction.hasOpened();

                refreshAuctionItem();
            break;

            case "end":  //auction ended
                auction.hasClosed();

                refreshAuctionItem();
            break;
        }
    }

    public void itemChange(JSONObject message)
    {
        String type = message.optString("type");
        switch(type)
        {
            case "high":
            {
                String auctionItemId = message.optString("auctionItemId");
                if (!auctionItemId.equals(auctionItem.id))
                    return;

                // This will be a high bid message
                String bidAmount        = message.optString("bidAmount");
                String userProfileId    = message.optString("userProfileId");

                auctionItem.topBidAmount        = Float.parseFloat(bidAmount);
                auctionItem.topBidUserProfileId = userProfileId;

                refreshBidInfo();
            }
            break;

            case "paid":
                auctionItem.hasPaid();
                refreshBidInfo();
            break;
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        doCleanUpResources();
    }

    private void doCleanUpResources() {
        mImagesArray.clear();
        auctionItem = null;
        auction = null;
        subscription = null;
    }

}
