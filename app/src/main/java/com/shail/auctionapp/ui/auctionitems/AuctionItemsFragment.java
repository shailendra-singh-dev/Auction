package com.shail.auctionapp.ui.auctionitems;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

import io.app..R;
import com.shail.auctionapp.apiclients.AuctionItemsClient;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.AlertBlock;
import com.shail.auctionapp.interfaces.CallBlock;
import com.shail.auctionapp.interfaces.IAspectRatioView;
import com.shail.auctionapp.interfaces.MessageBlock;
import com.shail.auctionapp.messaging.Subscription;
import com.shail.auctionapp.models.Asset;
import com.shail.auctionapp.models.Auction;
import com.shail.auctionapp.models.AuctionItem;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.ui.auctionitemdetail.AuctionItemDetailFragment;
import com.shail.auctionapp.ui.common.BaseFragment;
import com.shail.auctionapp.utils.AppConst;
import com.shail.auctionapp.utils.AppUtils;
import com.shail.auctionapp.views.AspectRatioRelativeLayout;
import com.shail.auctionapp.views.OnItemClickListener;
import com.shail.auctionapp.views.RecycleViewItemTouchListener;
import com.shail.auctionapp.views.RecyclerViewAdapter;


public class AuctionItemsFragment extends BaseFragment
{
    private static final String TAG         = AuctionItemsFragment.class.getSimpleName();
    private static final String AUCTION_ID  = "auction_id";

    private TextView                    mAuctionTitleView;

    private List<AuctionItem>   auctionItems;
    private Subscription        subscription;

    public Auction              auction;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;

    public AuctionItemsFragment() {
    }

    @Override
    protected String getScreenName() {
        return SCREEN.AUCTIONS_ITEMS.getScreenName();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

    }

    @Override
    public void onStart()
    {
        super.onStart();

        fetchAuctionItems();
        subscribe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_auction_items, container, false);

        mSwipeRefreshLayout= (SwipeRefreshLayout) parentView.findViewById(R.id.auction_items_swiperefreshlayout);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.background_dark);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        fetchAuctionItems();
                    }
                }
        );

        mAuctionTitleView = (TextView) parentView.findViewById(R.id.auction_title);

        recyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.auction_items_recyclerview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getAttachedActivity());

        recyclerView.setHasFixedSize(true);

        final RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(recyclerView) {

            @Override
            protected View createView(Context context, ViewGroup viewGroup, int viewType) {
                final AspectRatioRelativeLayout aspectRatioRelativeLayout = (AspectRatioRelativeLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.auction_items_row, viewGroup, false);
                aspectRatioRelativeLayout.setAspectRatioMode(IAspectRatioView.MAINTAIN_WIDTH);
                aspectRatioRelativeLayout.setAspectRatio(16, 9);
                return aspectRatioRelativeLayout;
            }

            @Override
            protected void bindView(int position, ViewHolder viewHolder) {
                AuctionItem auctionItem =auctionItems.get(position);
                if(null != auctionItem){
                    viewHolder.itemView.setTag(auctionItem);

                    ImageView auctionItemThumbnailView = (ImageView) viewHolder.getView(R.id.auction_item_thumbnail);

                    LinearLayout auctionItemTitleParentView = (LinearLayout) viewHolder.getView(R.id.auction_item_title_parent);

                    Typeface typeFace = AppUtils.getTypefaceFromFontFamily(viewHolder.itemView.getContext());

                    TextView auctionSponsoredByView = (TextView) viewHolder.getView(R.id.auction_sponsoredby);
                    auctionSponsoredByView.setTypeface(typeFace);

                    TextView auctionItemTitleView = (TextView) viewHolder.getView(R.id.auction_item_title);
                    auctionItemTitleView.setTypeface(typeFace);

                    auctionItemTitleView.setText(auctionItem.title);

                    String sponsoredBy = auction.getSponsoredBy();
                    if (null == sponsoredBy || sponsoredBy.isEmpty()) {
                        sponsoredBy = "";
                        auctionItemTitleView.setBackgroundResource(R.drawable.rectangle_grey);
                    }else{
                        auctionItemTitleParentView.setBackgroundResource(R.drawable.rectangle_grey);
                    }
                    auctionSponsoredByView.setText(sponsoredBy);

                    if (null != auctionItem.assets && 0 < auctionItem.assets.size()) {
                        final Asset asset = auctionItem.assets.get(0);
                        final String thumbnailUrl = Session.getInstance().ApiBaseURL + "assets/" + auction.id + "/download/" + asset.url;
                        Log.i(TAG, "auctionItem:" + auctionItem + ",thumbnailUrl:" + thumbnailUrl);

                        Picasso.with(viewHolder.itemView.getContext())
                                .load(thumbnailUrl)
                                .noFade()
                                .into(auctionItemThumbnailView);
                    } else {
                        auctionItemThumbnailView.setImageDrawable(null);
                    }
                }
            }

            @Override
            public int getItemCount() {
                return null == auctionItems ? 0 : auctionItems.size();
            }

            @Override
            public long getItemId(int position) {
                Object listItem = auctionItems.get(position);
                return listItem.hashCode();
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

        recyclerView.setAdapter(recyclerViewAdapter);
        setIOnDataAvailableListener(recyclerViewAdapter);

        recyclerView.addOnItemTouchListener(new RecycleViewItemTouchListener(getAttachedActivity(),
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                AuctionItem auctionItem =auctionItems.get(position);
                                launchAuctionItemDetailScreen(auctionItem);
                            }
                        }
                )
        );
        recyclerView.setLayoutManager(linearLayoutManager);

        return parentView;
    }


    public void launchAuctionItemDetailScreen(AuctionItem auctionItem)
    {
        AuctionItemDetailFragment fragment  = new AuctionItemDetailFragment();
        fragment.auctionItem                = auctionItem;
        fragment.auction                    = auction;

        getAttachedActivity().pushFragment(fragment);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        trackViewAuctionItems();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        unsubscribe();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateTopBar();

        updateAuctionItemsScreen();
    }

    public void trackViewAuctionItems()
    {
        Session session = Session.getInstance();

        JSONObject props = new JSONObject();
        try {
            props.put("auctionId", auction.id);
            props.put("auctionName", auction.title);
            props.put("userId", session.userProfileId);
        } catch (org.json.JSONException ex) {
        }

        session.analytics.track("ViewAuctionItems", props);
    }

    public void updateAuctionItemsScreen()
    {
        if (auction == null)
            return;

        mAuctionTitleView.setText(auction.title);

        mSwipeRefreshLayout.setRefreshing(false);

        if(null != getIOnDataAvailableListener()){
            getIOnDataAvailableListener().onDataAvailable();
        }
    }


    @Override
    protected void updateTopBar() {
        updateToolBarTitle(getScreenName());
    }

    private void fetchAuctionItems()
    {
        String auctionId = auction.id;

        progressShow(AppConst.PROGRESS_SHOW_MSG_DELAY);

        AuctionItemsClient.fetchAuctionItems(auctionId,
                new CallBlock<AuctionItem>() {
                    @Override
                    public void onSuccess(List<AuctionItem> response) {
                        progressHide();

                        auctionItems = response;

                        updateAuctionItemsScreen();
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        progressHide();

                        Alerts.okAlert(getAttachedActivity(), t.getMessage(),
                                new AlertBlock() {
                                    @Override
                                    public void onOk() {
                                        fetchAuctionItems();
                                    }
                                }
                        );
                    }
                }
        );
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
                            case "item":    bidChange(message);     break;
                        }
                    }
                }
        );
    }

    private void auctionChange(JSONObject message)
    {
        String auctionId = message.optString("auctionId");

        if (auction.id.equals(auctionId))
            fetchAuctionItems();
    }

    private AuctionItem auctionItemById(String auctionItemId)
    {
        for(AuctionItem item : auctionItems)
            if (item.id.equals(auctionItemId))
                return item;

        return null;
    }

    private void bidChange(JSONObject message)
    {
        String type = message.optString("type");
        if (!type.equals("high"))
            return;

        String auctionItemId    = message.optString("auctionItemId");
        AuctionItem item        = auctionItemById(auctionItemId);
        if (item == null)
            return;

        String bidAmount        = message.optString("bidAmount");
        String userProfileId    = message.optString("userProfileId");

        item.topBidAmount           = Float.parseFloat(bidAmount);
        item.topBidUserProfileId    = userProfileId;
    }

    private void unsubscribe()
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
        auction = null;
        if(null != auctionItems){
            auctionItems.clear();
        }
        auctionItems = null;
        recyclerView.setAdapter(null);
        subscription = null;
    }
}
