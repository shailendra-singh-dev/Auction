package com.shail.auctionapp.interfaces;

import android.support.annotation.IntDef;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface IAspectRatioView
{
    /**
     * Disables maintaining aspect ratio (default).
     *
     * @see #setAspectRatioMode(int)
     */
    int NO_ASPECT_RATIO = 0;
    /**
     * changes the height wile keeping the width.
     * expected to get MeasureSpec.EXACTLY for width and MeasureSpec.UNSPECIFIED for height
     *
     * @see #setAspectRatioMode(int)
     */
    int MAINTAIN_WIDTH = 1;
    /**
     * changes the width wile keeping the height.
     * expected to get MeasureSpec.EXACTLY for height and MeasureSpec.UNSPECIFIED for width
     *
     * @see #setAspectRatioMode(int)
     */
    int MAINTAIN_HEIGHT = 2;
    /**
     * changes width and height to fit the view boundary.
     * expected to get MeasureSpec.AT_MOST or MeasureSpec.UNSPECIFIED for both width and height
     *
     * @see #setAspectRatioMode(int)
     */
    int SCALE_TO_FIT = 3;
    /**
     * reduces either width or height to fit the measured view boundary.
     * expected to get MeasureSpec.AT_MOST or MeasureSpec.UNSPECIFIED for both width and height
     *
     * @see #setAspectRatioMode(int)
     */
    int SHRINK_TO_FIT = 4;

    /**
     * @hide
     */
    @IntDef({NO_ASPECT_RATIO, MAINTAIN_WIDTH, MAINTAIN_HEIGHT, SCALE_TO_FIT, SHRINK_TO_FIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AspectRatioMode {
    }

    class AspectRatioHelper implements IAspectRatioView {
        private static final int ASPECT_RATIO_MUL_FACTOR = 100000;
        private int mWidth = 0;
        private int mHeight = 0;
        private int mMeasuredWidth = 0;
        private int mMeasuredHeight = 0;
        private int mWidthSpecHint = 0;
        private int mHeightSpecHint = 0;
        private int mMaxDeformationPixel = 0;
        private int mAspectRatioMode = NO_ASPECT_RATIO;

        @Override
        public void setAspectRatio(final int width, final int height) {
            mWidth = width;
            mHeight = height;
        }

        @Override
        public void setAspectRatio(final double aspectRatio) {
            mWidth = (int)((double)ASPECT_RATIO_MUL_FACTOR * aspectRatio);
            mHeight = ASPECT_RATIO_MUL_FACTOR;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        public int getMeasuredWidth() {
            return mMeasuredWidth;
        }

        public int getMeasuredHeight() {
            return mMeasuredHeight;
        }

        public int getWidthSpecHint() {
            return mWidthSpecHint;
        }

        public int getHeightSpecHint() {
            return mHeightSpecHint;
        }

        public int getAspectRatioMode() {
            return mAspectRatioMode;
        }

        @Override
        public void setAspectRatioMode(@AspectRatioMode final int aspectRatioMode) {
            mAspectRatioMode = aspectRatioMode;
        }

        @Override
        public void setMaxDeformationPixel(final int maxDeformationPixel) {
            mMaxDeformationPixel = maxDeformationPixel;
        }

        /**
         * Measure the view with configured aspect ratio. This function to be used if super.onMeasure() is not called.
         *
         * @param widthSpec  horizontal space requirements as imposed by the parent.
         *                   The requirements are encoded with
         *                   {@link android.view.View.MeasureSpec}.
         * @param heightSpec vertical space requirements as imposed by the parent.
         *                   The requirements are encoded with
         *                   {@link android.view.View.MeasureSpec}.
         */
        public void measureForAspectRatio(final int widthSpec, final int heightSpec) {
            measureForAspectRatio(widthSpec, heightSpec, View.MeasureSpec.getSize(widthSpec),
                    View.MeasureSpec.getSize(heightSpec));
        }

        /**
         * Measure the view with configured aspect ratio. This function to be used if super.onMeasure() is called and
         * measured width and height is available.
         *
         * @param widthSpec      horizontal space requirements as imposed by the parent.
         *                       The requirements are encoded with
         *                       {@link android.view.View.MeasureSpec}.
         * @param heightSpec     vertical space requirements as imposed by the parent.
         *                       The requirements are encoded with
         *                       {@link android.view.View.MeasureSpec}.
         * @param measuredWidth  measured width as per super.onMeasure()
         * @param measuredHeight measured height as per super.onMeasure()
         */
        public void measureForAspectRatio(final int widthSpec, final int heightSpec, final int measuredWidth,
                                          final int measuredHeight) {
            if ((mWidth <= 0) || (mHeight <= 0) || (mAspectRatioMode == NO_ASPECT_RATIO)) {
                mMeasuredWidth = measuredWidth;
                mMeasuredHeight = measuredHeight;
                mWidthSpecHint = widthSpec;
                mHeightSpecHint = heightSpec;
                return;
            }
            final int calculatedWidth;
            final int calculatedHeight;
            if (mAspectRatioMode == MAINTAIN_WIDTH) {
                calculatedHeight = (measuredWidth * mHeight) / mWidth;
                calculatedWidth = measuredWidth;
            } else if (mAspectRatioMode == MAINTAIN_HEIGHT) {
                calculatedWidth = (measuredHeight * mWidth) / mHeight;
                calculatedHeight = measuredHeight;
            } else {
                final int boundWidth;
                final int boundHeight;
                if (mAspectRatioMode == SCALE_TO_FIT) {
                    boundWidth = View.MeasureSpec.getSize(widthSpec);
                    boundHeight = View.MeasureSpec.getSize(heightSpec);
                } else {
                    boundWidth = measuredWidth;
                    boundHeight = measuredHeight;
                }
                final int reqAspectRatio = (mWidth * ASPECT_RATIO_MUL_FACTOR) / mHeight;
                final int viewAspectRatio = (boundWidth * ASPECT_RATIO_MUL_FACTOR) / boundHeight;
                if (viewAspectRatio > reqAspectRatio) {
                    calculatedHeight = boundHeight;
                    calculatedWidth = (boundHeight * mWidth) / mHeight;
                } else {
                    calculatedWidth = boundWidth;
                    calculatedHeight = (boundWidth * mHeight) / mWidth;
                }
            }
            if (Math.abs(calculatedWidth - measuredWidth) > mMaxDeformationPixel) {
                mMeasuredWidth = calculatedWidth;
            } else {
                mMeasuredWidth = measuredWidth;
            }
            if (Math.abs(calculatedHeight - measuredHeight) > mMaxDeformationPixel) {
                mMeasuredHeight = calculatedHeight;
            } else {
                mMeasuredHeight = measuredHeight;
            }
            int widthMode = View.MeasureSpec.getMode(widthSpec);
            int heightMode = View.MeasureSpec.getMode(heightSpec);
            if(widthMode != View.MeasureSpec.EXACTLY){
                widthMode = View.MeasureSpec.AT_MOST;
            }
            if(heightMode != View.MeasureSpec.EXACTLY){
                heightMode = View.MeasureSpec.AT_MOST;
            }
            mWidthSpecHint = View.MeasureSpec.makeMeasureSpec(mMeasuredWidth, widthMode);
            mHeightSpecHint = View.MeasureSpec.makeMeasureSpec(mMeasuredHeight, heightMode);
        }
    }

    /**
     * Set the aspect ratio to maintain for this view.
     *
     * @param width  width value for aspect ratio
     * @param height height value for aspect ratio
     */
    void setAspectRatio(final int width, final int height);

    /**
     * Set the aspect ratio to maintain for this view.
     *
     * @param aspectRatio aspect ratio. width and height value for aspect ratio is calculated internally by
     *                    multiplying the value by constant factor. Internal calculation are done using int.
     *                    so it is better to use other interface @see #setAspectRatio(int,int) if int values
     *                    are already known.
     */
    void setAspectRatio(final double aspectRatio);

    /**
     * Control how items are fitted to occupy their space.
     *
     * @param aspectRatioMode Either {@link #NO_ASPECT_RATIO},
     *                        {@link #MAINTAIN_WIDTH}, {@link #SCALE_TO_FIT}, or {@link #MAINTAIN_HEIGHT}.
     */
    public void setAspectRatioMode(@AspectRatioMode final int aspectRatioMode);

    /**
     * Maximum deformation tolerance allowed to keep the width and height
     *
     * @param maxDeformationPixel Tolerance for changing aspect ratio. If the change to width/height is <= maxDeformationPixel then
     *                            change will not be done. default is 0.
     */
    public void setMaxDeformationPixel(final int maxDeformationPixel);
}
