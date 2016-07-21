package com.shail.auctionapp.application;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.shail.auctionapp.BuildConfig;
import com.shail.auctionapp.apiclients.Session;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;


public class AuctionApplication extends Application
{
    private static final String LOG = Application.class.getSimpleName();
    private static final long DISK_CACHE_SIZE = 100 * 1024 * 1024;

    private static Application mApp = null;

    @Override
    public void onCreate()
    {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        setInstance(this);

        //Setup Analytics
        Session session     = Session.getInstance();
        session.appContext  = this;
        session.analytics   = MixpanelAPI.getInstance(this, BuildConfig.MIXPANEL_ID); //DEBUG

        session.recoverSession();

        initPicasso();
    }

    @Override
    public void onLowMemory()
    {
        Log.d(LOG, "onLowMemory");
        super.onLowMemory();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(final int level)
    {
        Log.d(LOG, "onTrimMemory:" + level);
        super.onTrimMemory(level);
    }

    private void initPicasso(){
        Picasso.Builder builder = new Picasso.Builder(this);
        OkHttpDownloader okHttpDownloader = new OkHttpDownloader(this, DISK_CACHE_SIZE);
        builder.downloader(okHttpDownloader);
        Picasso picasso = builder.build();
        Picasso.setSingletonInstance(picasso);
    }

    private static void setInstance(final Application app)
    {
        mApp = app;
    }

    public static Application getInstance()
    {
        return mApp;
    }
}
