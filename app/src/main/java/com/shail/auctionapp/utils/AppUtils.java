package com.shail.auctionapp.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import java.text.DecimalFormat;

import io.app..BuildConfig;
import io.app..R;

public class AppUtils
{
    public static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = 30000;
    public static final long DEFAULT_READ_TIMEOUT_MILLIS = 30000;
    public static final long DEFAULT_WRITE_TIMEOUT_MILLIS = 30000;

    private AppUtils()
    {
    }

    public static String getAppVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getAppBuildType(){
        return BuildConfig.BUILD_TYPE;
    }

    public static double getAuctionsScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static double getAuctionsScreenHeight() {
        return getAuctionsScreenWidth() * AppConst.AUCTIONS_SCREEN_ROW_ASPECT_RATIO;
    }

    private static DecimalFormat bidFormatter;

    public static String getFloatFormatFromStringId(final Context context, final int stringId, final float value)
    {
        if (bidFormatter == null)
        {
            bidFormatter = new DecimalFormat("#,###.00");

            bidFormatter.setMinimumFractionDigits(0);
            bidFormatter.setMaximumFractionDigits(2);
        }

        String result = "";
        try
        {
            result = String.format(context.getString(stringId), bidFormatter.format(value));
        }
        catch (Exception exception) {
        }

        return result;
    }

    public static float getFloatFromString(final String value) {
        float result = 0.00f;
        try
        {
            result = Float.parseFloat(value);
        }
        catch (Exception exception)
        {
        }

        return result;
    }

    public static Bitmap getAspectRatioImageBitmap(Bitmap bitmap, int targetdHeight) {
        int bmWidth = bitmap.getWidth();
        int bmHeight = bitmap.getHeight();
        int targetWidth = calculateProportionalWidth(bmHeight, bmWidth, targetdHeight);
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetdHeight, false);
    }

    private static int calculateProportionalWidth(int imageHeight, int imgeWidth, int targetHeight) {
        return (int) (((float) targetHeight / imageHeight) * imgeWidth);
    }

    public static Typeface getTypefaceFromFontFamily(Context context) {
        String fontFamilySuffix = ".ttf";
        return Typeface.createFromAsset(context.getAssets(), "fonts/" + context.getString(R.string.typeface_font_family_roboto) + fontFamilySuffix);
    }

    /**
     * Compares two version strings.
     *
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     *         The result is a positive integer if str1 is _numerically_ greater than str2.
     *         The result is zero if the strings are _numerically_ equal.
     */
    public static Integer versionCompare(String str1, String str2)
    {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i]))
        {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length)
        {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        else
        {
            return Integer.signum(vals1.length - vals2.length);
        }
    }

    @SuppressWarnings(AppConst.DEPRECATION)
    public static void setBackgroundDrawable(final View view, final Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            setBackgroundV16(view, drawable);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setBackgroundV16(final View view, final Drawable bd) {
        view.setBackground(bd);
    }
}
