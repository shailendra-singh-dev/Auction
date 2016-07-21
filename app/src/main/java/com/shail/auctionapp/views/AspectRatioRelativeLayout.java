package com.shail.auctionapp.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.shail.auctionapp.interfaces.IAspectRatioView;

public class AspectRatioRelativeLayout extends RelativeLayout implements IAspectRatioView {

    private final AspectRatioHelper mAspectRatioHelper = new AspectRatioHelper();
    public AspectRatioRelativeLayout(final Context context) {
        super(context);
    }

    public AspectRatioRelativeLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioRelativeLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AspectRatioRelativeLayout(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setAspectRatio(final int width, final int height) {
        mAspectRatioHelper.setAspectRatio(width, height);
        requestLayout();
    }

    @Override
    public void setAspectRatio(final double aspectRatio) {
        mAspectRatioHelper.setAspectRatio(aspectRatio);
        requestLayout();
    }

    @Override
    public void setAspectRatioMode(@AspectRatioMode final int aspectRatioMode) {
        mAspectRatioHelper.setAspectRatioMode(aspectRatioMode);
        requestLayout();
    }

    @Override
    public void setMaxDeformationPixel(final int maxDeformationPixel) {
        mAspectRatioHelper.setMaxDeformationPixel(maxDeformationPixel);
        requestLayout();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        mAspectRatioHelper.measureForAspectRatio(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(mAspectRatioHelper.getWidthSpecHint(), mAspectRatioHelper.getHeightSpecHint());
        mAspectRatioHelper.measureForAspectRatio(widthMeasureSpec, heightMeasureSpec, getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(mAspectRatioHelper.getMeasuredWidth(), mAspectRatioHelper.getMeasuredHeight());
    }
}
