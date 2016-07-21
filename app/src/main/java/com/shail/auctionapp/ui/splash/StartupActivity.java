package com.shail.auctionapp.ui.splash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import io.app..BuildConfig;
import io.app..R;
import com.shail.auctionapp.apiclients.ConfigClient;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.AlertBlock;
import com.shail.auctionapp.interfaces.CallBlock;
import com.shail.auctionapp.models.Config;
import com.shail.auctionapp.ui.common.BaseActivity;
import com.shail.auctionapp.ui.common.Activity;
import com.shail.auctionapp.utils.AppConst;
import com.shail.auctionapp.utils.AppUtils;

public class StartupActivity extends BaseActivity implements View.OnClickListener
{
    private static final String TAG = StartupActivity.class.getSimpleName();

    private TextView    introText;
    private Button      introButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        introText   = (TextView)findViewById(R.id.intro_text);

        introButton = (Button)findViewById(R.id.intro_button);
        introButton.setOnClickListener(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        fetchConfig();
    }

    public void fetchConfig()
    {

        progressShow(AppConst.PROGRESS_SHOW_MSG_NOW);

        ConfigClient.fetchConfigDetails(BuildConfig._CONFIG_NAME,
            new CallBlock<Config>()
            {
                @Override
                public void onSuccess(final List<Config> response)
                {
                    Log.d(TAG, "fetchConfig() onSuccess");
                    progressHide();

                    //Configuration setup
                    Config config       = response.get(0);
                    Session session     = Session.getInstance();
                    session.config      = config;
                    session.ApiBaseURL  = config.apiBaseURL + "/";

                    //Version Check
                    if (appVersionIsGood(config))
                        checkForIntro();
                }

                @Override
                public void onFailure(final Throwable t)
                {
                    Log.d(TAG, "fetchConfig() onFailure");
                    progressHide();

                    Alerts.okAlert(StartupActivity.this, t.getMessage(),
                        new AlertBlock()
                        {
                            @Override
                            public void onOk()
                            {
                                fetchConfig(); //try again
                            }
                        }
                    );
                }
            }
        );
    }

    // Intro

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.intro_button:
                Session.getInstance().introDone();
                authenticate();
            break;
        }
    }

    public void checkForIntro()
    {
        Session session = Session.getInstance();

        if (!session.isAppIntroDone())
        {
            introText.setVisibility(View.VISIBLE);
            introButton.setVisibility(View.VISIBLE);
        }
        else
            authenticate();
    }

    public void authenticate()
    {
        // Authentication TBD and on successful completion
        authenticated();
    }

    public void authenticated()
    {
        Session session = Session.getInstance();

        session.startSessionServices();

        start();
    }

    public void start()
    {
        Intent intent = new Intent(StartupActivity.this, Activity.class);
        StartupActivity.this.startActivity(intent);
        StartupActivity.this.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // App Config Check

    public void sendToGooglePlay()
    {
        Log.d("startup", "sending user to Google Play Store.");

        // market://details?id=io.app.
        String appUrl   = Session.getInstance().config.googlePlayURL;
        Intent browser  = new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl));

        try
        {
            startActivity(browser);
        }
        catch(Exception ex)
        {
            Alerts.okAlert(this, "Error", "Unable to send you to the Google Play store to update your app.", null);
        }
    }

    public boolean appVersionIsGood(Config config)
    {
        String appVersion       = AppUtils.getAppVersionName();
        String minVersion       = config.androidMinVersion;
        String currentVersion   = config.androidCurrentVersion;

        Log.d("startup", "app=" + appVersion + ", min=" + minVersion + ", cur=" + currentVersion);

        // if appVersion > currentVersion then we have a development version
        if (AppUtils.versionCompare(appVersion, currentVersion) > 0)
        {
            Log.d("startup", "development version");
            return true;
        }

        // if appVersion == currentVersion then we're current
        if (AppUtils.versionCompare(appVersion, currentVersion) == 0)
        {
            Log.d("startup", "app is current");
            return true;
        }

        // if appVersion < minVersion then force update & send them to iTunes
        if (AppUtils.versionCompare(appVersion, minVersion) < 0)
        {
            Log.d("startup", "app requires update");

            Alerts.okAlert(this, "Update Now", "Please update this app.",
                new AlertBlock()
                {
                    @Override
                    public void onOk()
                    {
                        sendToGooglePlay();
                    }
                }
            );

            return false;
        }

        Log.d("startup", "app update is available");

        // Assume appVersion < currentVersion so alert them an update is available.
        Alerts.YesNoAlert(this, "Update Now?", "A newer version of the app is available.",
                new AlertBlock() {
                    @Override
                    public void onOk() {
                        sendToGooglePlay();
                    }

                    @Override
                    public void onNo() {
                        checkForIntro();
                }
            }
        );

        return false;
    }
}
