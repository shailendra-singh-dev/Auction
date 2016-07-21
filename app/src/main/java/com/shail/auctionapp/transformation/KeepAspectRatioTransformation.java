package com.shail.auctionapp.transformation;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Transformation;

import com.shail.auctionapp.utils.AppConst;

public class KeepAspectRatioTransformation implements Transformation
{
    private static final String TAG = KeepAspectRatioTransformation.class.getSimpleName();
    private final ImageView mImageView;

    public KeepAspectRatioTransformation(final ImageView imageView) {
        mImageView = imageView;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        double aspectRatio = AppConst.AUCTIONS_SCREEN_ROW_ASPECT_RATIO;//maintain 16x9 aspect ratio
        int targetWidth = mImageView.getWidth();
        int targetHeight = (int) (targetWidth * aspectRatio);
        Log.i(TAG, "targetWidth:" + targetWidth + ",targetHeight:" + targetHeight);
        Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
        if (result != source) {
            // Same bitmap is returned if sizes are the same
            source.recycle();
        }
        return result;
    }

    @Override
    public String key() {
        return "transformation" + "aspect-ratio";
    }
}
