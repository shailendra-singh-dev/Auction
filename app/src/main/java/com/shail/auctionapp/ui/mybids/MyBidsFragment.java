package com.shail.auctionapp.ui.mybids;

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
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.app..R;
import com.shail.auctionapp.apiclients.AuctionItemsClient;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.apiclients.UserProfileClient;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.AlertBlock;
import com.shail.auctionapp.interfaces.CallBlock;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.interfaces.IAspectRatioView;
import com.shail.auctionapp.interfaces.MessageBlock;
import com.shail.auctionapp.messaging.Subscription;
import com.shail.auctionapp.models.Asset;
import com.shail.auctionapp.models.AuctionItem;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.models.UserBid;
import com.shail.auctionapp.ui.auctionitemdetail.AuctionItemDetailFragment;
import com.shail.auctionapp.ui.common.BaseFragment;
import com.shail.auctionapp.utils.AppConst;
import com.shail.auctionapp.utils.AppUtils;
import com.shail.auctionapp.views.AspectRatioRelativeLayout;
import com.shail.auctionapp.views.OnItemClickListener;
import com.shail.auctionapp.views.RecycleViewItemTouchListener;
import com.shail.auctionapp.views.RecyclerViewAdapter;

public class MyBidsFragment extends BaseFragment
{
    private static final String TAG = MyBidsFragment.class.getSimpleName();

    final private List<AuctionItem>       mAuctionItemList = new ArrayList<AuctionItem>();
    private TextView mMyBidsNotificationView;

    private Subscription subscription;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;

    public MyBidsFragment()
    {
    }

    @Override
    protected String getScreenName() {
        return SCREEN.MY_BIDS.getScreenName();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_my_bids, container, false);

        mMyBidsNotificationView = (TextView) parentView.findViewById(R.id.mybids_notification);

        mSwipeRefreshLayout= (SwipeRefreshLayout) parentView.findViewById(R.id.mybids_swiperefreshlayout);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.background_dark);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        fetchUserProfileBids();
                    }
                }
        );

        recyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.mybids_recyclerview);

        final RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(recyclerView) {
            @Override
            protected View createView(Context context, ViewGroup viewGroup, int viewType) {
                final AspectRatioRelativeLayout aspectRatioRelativeLayout = (AspectRatioRelativeLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mybids_items_row, viewGroup, false);
                aspectRatioRelativeLayout.setAspectRatioMode(IAspectRatioView.MAINTAIN_WIDTH);
                aspectRatioRelativeLayout.setAspectRatio(16, 9);
                return aspectRatioRelativeLayout;
            }

            @Override
            protected void bindView(int position, ViewHolder viewHolder) {
                AuctionItem auctionItem =mAuctionItemList.get(position);
                if(null != auctionItem){

                    viewHolder.itemView.setTag(auctionItem);

                    Typeface typeFace = AppUtils.getTypefaceFromFontFamily(viewHolder.itemView.getContext());

                    TextView myBidsStatusView = (TextView) viewHolder.getView(R.id.my_bids_status_text);
                    myBidsStatusView.setTypeface(typeFace);

                    TextView myBidsTitleView = (TextView) viewHolder.getView(R.id.mybids_title);
                    myBidsTitleView.setTypeface(typeFace);

                    ImageView myBidsThumbnailView = (ImageView) viewHolder.getView(R.id.mybids_thumbnail);


                    viewHolder.itemView.setTag(auctionItem);
                    myBidsTitleView.setText(auctionItem.title);

                    myBidsStatusView.setText(auctionItem.getBidStatusText(viewHolder.itemView.getContext()));

                    viewHolder.itemView.setTag(auctionItem);

                    if (null != auctionItem.assets && 0 < auctionItem.assets.size()) {
                        final Asset asset = auctionItem.assets.get(0);
                        final String thumbnailUrl = Session.getInstance().ApiBaseURL + "assets/" + auctionItem.auctionId + "/download/" + asset.url;
                        Log.i(TAG, "onBindViewHolder() auctionItem:" + auctionItem + ",thumbnailUrl:" + thumbnailUrl);

                        Picasso.with(viewHolder.itemView.getContext())
                                .load(thumbnailUrl)
                                .noFade()
                                .into(myBidsThumbnailView);

                    } else {
                        myBidsThumbnailView.setImageDrawable(null);
                    }
                }
            }

            @Override
            public int getItemCount() {
                return mAuctionItemList.size();
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

        recyclerView.addOnItemTouchListener(new RecycleViewItemTouchListener(getAttachedActivity(),
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                AuctionItem auctionItem = mAuctionItemList.get(position);
                                launchAuctionItemDetailScreen(auctionItem);
                            }
                        }
                )
        );

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getAttachedActivity());

        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(mLayoutManager);

        return parentView;
    }

    public void launchAuctionItemDetailScreen(final AuctionItem auctionItem)
    {
        AuctionItemDetailFragment fragment = new AuctionItemDetailFragment();
        fragment.auctionItem                = auctionItem;

        getAttachedActivity().pushFragment(fragment);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        fetchUserProfileBids();

        subscribe();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateTopBar();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        unsubscribe();
    }

    private void fetchUserProfileBids()
    {
        String userProfileId = Session.getInstance().userProfileId;

        progressShow(AppConst.PROGRESS_SHOW_MSG_DELAY);
        UserProfileClient.fetchUserProfileBidsWithId(userProfileId,
                new CallBlock<UserBid>()
                {
                    @Override
                    public void onSuccess(List<UserBid> response)
                    {
                        progressHide();

                        if(null != mAuctionItemList){
                            mAuctionItemList.clear();
                        }

                        for (UserBid userBid : response)
                        {
                            AuctionItem auctionItem = userBid.auctionItem;

                            if (null != mAuctionItemList && auctionItem != null)
                                mAuctionItemList.add(auctionItem);
                        }

                        Log.d(TAG, "fetchUserProfileBidsWithId mAuctionItemList:" + mAuctionItemList);
                        updateMyBidsScreen();

                        fetchAuctionItemAssets();
                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        progressHide();

                        Alerts.okAlert(getAttachedActivity(), t.getMessage(),
                            new AlertBlock()
                            {
                                @Override
                                public void onOk() {
                                    fetchUserProfileBids();
                                }
                            }
                        );
                    }
                }
        );
    }

    private void fetchAuctionItemAssets()
    {
        for(final AuctionItem item : mAuctionItemList)
        {
            AuctionItemsClient.fetchAuctionItemById(item.id,
                new CallBlockOne<AuctionItem>()
                {
                    @Override
                    public void onSuccess(AuctionItem response)
                    {
                        item.assets = response.assets;
                        updateMyBidsScreen();

                    }

                    @Override
                    public void onFailure(Throwable t)
                    {
                        //Ignore the lost auctionItem...
                    }
                }
            );
        }
    }

    private void refreshAuctionList(AuctionItem item)
    {
        //would prefer to check to see if tile is visible and then refresh
        updateMyBidsScreen();
    }

    private void updateMyBidsScreen()
    {
        mSwipeRefreshLayout.setRefreshing(false);

        mMyBidsNotificationView.setVisibility(mAuctionItemList.isEmpty() ? View.VISIBLE : View.GONE);

        if(null != getIOnDataAvailableListener()){
            getIOnDataAvailableListener().onDataAvailable();
        }
    }

    @Override
    protected void updateTopBar()
    {
        updateToolBarTitle(getScreenName());
    }

    // messaging

    private AuctionItem auctionItemById(String auctionItemId)
    {
        for(AuctionItem item : mAuctionItemList)
            if (item.id.equals(auctionItemId))
                return item;

        return null;
    }

    private boolean hasAuctionId(String auctionId)
    {
        for(AuctionItem item : mAuctionItemList)
            if (item.auctionId.equals(auctionId))
                return true;

        return false;
    }

    public void auctionChange(JSONObject message)
    {
        //If an auction we've bid on has changed, update the list.
        if (!hasAuctionId(message.optString("auctionId")))
            return;

        fetchUserProfileBids();
    }

    public void itemChange(JSONObject message)
    {
        AuctionItem item = auctionItemById(message.optString("auctionItemId"));
        if (item == null)
            return;

        String type = message.optString("type");
        switch(type)
        {
            case "high":
            {
                String bidAmount        = message.optString("bidAmount");
                String userProfileId    = message.optString("userProfileId");

                item.topBidAmount           = Float.parseFloat(bidAmount);
                item.topBidUserProfileId    = userProfileId;
            }
            break;

            case "paid":
            {
                item.hasPaid();
            }
            break;
        }

        refreshAuctionList(item);
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
        if(null != mAuctionItemList){
            mAuctionItemList.clear();
        }
        recyclerView.setAdapter(null);
        subscription = null;
    }
}
