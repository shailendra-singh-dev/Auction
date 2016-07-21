package com.shail.auctionapp.ui.auctions;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

import io.app..R;
import com.shail.auctionapp.apiclients.AuctionsClient;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.AlertBlock;
import com.shail.auctionapp.interfaces.CallBlock;
import com.shail.auctionapp.interfaces.IAspectRatioView;
import com.shail.auctionapp.interfaces.MessageBlock;
import com.shail.auctionapp.messaging.MessageManager;
import com.shail.auctionapp.messaging.Subscription;
import com.shail.auctionapp.models.Asset;
import com.shail.auctionapp.models.Auction;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.ui.auctionitems.AuctionItemsFragment;
import com.shail.auctionapp.ui.common.BaseFragment;
import com.shail.auctionapp.ui.more.MoreFragment;
import com.shail.auctionapp.ui.mybids.MyBidsFragment;
import com.shail.auctionapp.utils.AppConst;
import com.shail.auctionapp.utils.AppUtils;
import com.shail.auctionapp.views.AspectRatioRelativeLayout;
import com.shail.auctionapp.views.OnItemClickListener;
import com.shail.auctionapp.views.RecycleViewItemTouchListener;
import com.shail.auctionapp.views.RecyclerViewAdapter;


public class AuctionsFragment extends BaseFragment
{
    private static final String TAG = AuctionsFragment.class.getSimpleName();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<Auction> mAuctionList;
    private Subscription subscription;
    private RecyclerView recyclerView;

    public AuctionsFragment() {
    }

    @Override
    protected String getScreenName() {
        return SCREEN.AUCTIONS.getScreenName();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        fetchAuctions();
    }

    @Override
    public void onStop() {
        super.onStop();

        unsubscribe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_auctions, container, false);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.background_dark);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        fetchAuctions();
                    }
                }
        );

        return mSwipeRefreshLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.auctions_recyclerview);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getAttachedActivity());

        recyclerView.setHasFixedSize(true);

        final RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(recyclerView) {

            @Override
            protected View createView(Context context, ViewGroup viewGroup, int viewType) {
                final AspectRatioRelativeLayout aspectRatioRelativeLayout = (AspectRatioRelativeLayout) LayoutInflater.from(context).inflate(R.layout.auctions_row, viewGroup, false);
                aspectRatioRelativeLayout.setAspectRatioMode(IAspectRatioView.MAINTAIN_WIDTH);
                aspectRatioRelativeLayout.setAspectRatio(16, 9);
                return aspectRatioRelativeLayout;
            }

            @Override
            protected void bindView(int position, ViewHolder viewHolder) {
                if(position < mAuctionList.size()){
                    final Auction auction = mAuctionList.get(position);
                    if (auction != null) {
                        viewHolder.itemView.setTag(auction);

                        Typeface typeFace = AppUtils.getTypefaceFromFontFamily(viewHolder.itemView.getContext());
                        final TextView auctionTitleView = (TextView) viewHolder.getView(R.id.auction_title);
                        auctionTitleView.setTypeface(typeFace);
                        auctionTitleView.setText(auction.title);

                        ImageView auctionThumbnailView = (ImageView) viewHolder.getView(R.id.auction_thumbnail);

                        if (0 < auction.assets.size()) {
                            final Asset asset = auction.assets.get(0);
                            final String thumbnailUrl = Session.getInstance().ApiBaseURL + "assets/" + auction.id + "/download/" + asset.url;
                            Log.i(TAG, "auction:" + auction + ",thumbnailUrl:" + thumbnailUrl);

                            Picasso.with(viewHolder.itemView.getContext())
                                    .load(thumbnailUrl)
                                    .noFade()
                                    .into(auctionThumbnailView);
                        } else {
                            auctionThumbnailView.setImageDrawable(null);
                        }
                    }
                }
            }

            @Override
            public int getItemCount() {
                return null == mAuctionList ? 0 : mAuctionList.size();
            }

            @Override
            public long getItemId(int position) {
                Object listItem = mAuctionList.get(position);
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

        setIOnDataAvailableListener(recyclerViewAdapter);
        recyclerView.setAdapter(recyclerViewAdapter);

        recyclerView.addOnItemTouchListener(new RecycleViewItemTouchListener(getAttachedActivity(),
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                AuctionItemsFragment fragment = new AuctionItemsFragment();
                                Auction auction = mAuctionList.get(position);
                                fragment.auction = auction;
                                getAttachedActivity().pushFragment(fragment);
                            }
                        }
                )
        );
        recyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_auctions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_my_bids:
                launchMyBidsScreen();
                break;

            case R.id.action_more:
                launchMoreScreen();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchMyBidsScreen() {
        MyBidsFragment fragment = new MyBidsFragment();

        getAttachedActivity().pushFragment(fragment);
    }

    public void launchMoreScreen() {
        MoreFragment fragment = new MoreFragment();

        getAttachedActivity().pushFragment(fragment);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateTopBar();
    }

    @Override
    protected void updateTopBar() {
        updateToolBarTitle(getScreenName());
    }

    public void fetchAuctions() {
        progressShow(AppConst.PROGRESS_SHOW_MSG_DELAY);

        AuctionsClient.fetchAuctionList(
                new CallBlock<Auction>() {
                    @Override
                    public void onSuccess(List<Auction> response) {
                        progressHide();

                        mAuctionList = response;

                        updateAuctionsScreen();

                        //Since this is the first screen, we need to defer the setup of the subscription until
                        //  after the messaging system is initialized.  We should have that by this point.
                        subscribe();
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        progressHide();

                        Alerts.okAlert(getAttachedActivity(), t.getMessage(),
                                new AlertBlock() {
                                    @Override
                                    public void onOk() {
                                        fetchAuctions();
                                    }
                                }
                        );
                    }
                }
        );
    }

    private void updateAuctionsScreen() {
        mSwipeRefreshLayout.setRefreshing(false);

        if(null != getIOnDataAvailableListener()){
            getIOnDataAvailableListener().onDataAvailable();
        }
    }

    private void auctionChange() {
        //An auction has started, ended or changed to a pending state, so let's...
        fetchAuctions();
    }

    public void subscribe() {
        if (subscription != null)
            return;

        MessageManager manager = Session.getInstance().messageManager;

        subscription = manager.subscribeTo("bids",
                new MessageBlock() {
                    @Override
                    public void onMessage(JSONObject message) {
                        String group = message.optString("group");

                        if (group.equals("auction"))
                            auctionChange();
                    }
                }
        );
    }

    public void unsubscribe() {
        Session.getInstance().messageManager.unsubscribe(subscription);
        subscription = null;
    }

}
