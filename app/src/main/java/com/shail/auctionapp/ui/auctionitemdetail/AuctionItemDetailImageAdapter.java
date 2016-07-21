package com.shail.auctionapp.ui.auctionitemdetail;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.app..R;
import com.shail.auctionapp.interfaces.IAspectRatioView;
import com.shail.auctionapp.views.AspectRatioRelativeLayout;


public class AuctionItemDetailImageAdapter extends PagerAdapter
{
    private static final String TAG = AuctionItemDetailImageAdapter.class.getSimpleName();

    private ArrayList<String>   mImageArrayList;

    public AuctionItemDetailImageAdapter(ArrayList<String> imageArrayList)
    {
        mImageArrayList = imageArrayList;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mImageArrayList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position)
    {

        final AspectRatioRelativeLayout aspectRatioRelativeLayout = (AspectRatioRelativeLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_auction_item_details_images, viewGroup, false);
        aspectRatioRelativeLayout.setAspectRatioMode(IAspectRatioView.MAINTAIN_WIDTH);
        aspectRatioRelativeLayout.setAspectRatio(16, 9);

        final ImageView imageView = (ImageView) aspectRatioRelativeLayout.findViewById(R.id.viewpager_item_thumbnail);
        final String thumbnailUrl = mImageArrayList.get(position);
        Log.i(TAG, "instantiateItem(),thumbnailUrl:" + thumbnailUrl);

        Picasso.with(viewGroup.getContext())
                .load(thumbnailUrl)
                .noFade()
                .into(imageView);

        viewGroup.addView(aspectRatioRelativeLayout, 0);

        return aspectRatioRelativeLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view.equals(object);
    }
}
