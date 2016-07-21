package com.shail.auctionapp.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;

import com.shail.auctionapp.utils.AppUtils;

public class AuctionDetailsImageViewPager extends ViewPager
{
    private static final String TAG = AuctionDetailsImageViewPager.class.getSimpleName();

    public AuctionDetailsImageViewPager(final Context context) {
        super(context);
    }

    public AuctionDetailsImageViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = getMeasuredWidth();
        final int height = (int) AppUtils.getAuctionsScreenHeight();
        Log.i(TAG, "onMeasure():" + height);
        setMeasuredDimension(width, height);
    }
}
