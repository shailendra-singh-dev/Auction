package com.shail.auctionapp.ui.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.app..R;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.models.Config;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.ui.common.BaseFragment;

public class AboutFragment extends BaseFragment
{
    private static final String TAG = AboutFragment.class.getSimpleName();

    @Override
    protected String getScreenName()
    {
        return SCREEN.ABOUT.getScreenName();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        updateTopBar();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        final WebView webView = (WebView) view.findViewById(R.id.webview);

        String aboutUrl = getAboutURL();
        Log.i(TAG, "aboutUrl:" + aboutUrl);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new MyWebViewClient());
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int progress) {
                //Handle Loading progres Data..
                Log.i(TAG, "onProgressChanged(),progress:" + progress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                Log.i(TAG, "onProgressChanged(),title:" + title);
            }
        });

        webView.loadUrl(aboutUrl);

        return view;
    }


    @Override
    protected void updateTopBar()
    {
        updateToolBarTitle(getScreenName());
    }

    private String getAboutURL()
    {
        final Session session = Session.getInstance();
        final Config config = session.config;
        final String aboutUrl = config.aboutURL;
        return aboutUrl;
    }

    private class MyWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String aboutUrl = getAboutURL();
            if (Uri.parse(url).getHost().equals(aboutUrl)) {
                Log.i(TAG, "shouldOverrideUrlLoading");
                // This is your web site, so do not override; let the WebView to load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Log.i(TAG, "intent");
            startActivity(intent);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            Log.i(TAG, "onReceivedSslError");
            // this will ignore the Ssl error and will go forward to your site
            handler.proceed();
        }
    }
}
