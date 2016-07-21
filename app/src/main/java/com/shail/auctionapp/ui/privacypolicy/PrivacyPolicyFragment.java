package com.shail.auctionapp.ui.privacypolicy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import io.app..R;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.ui.common.BaseFragment;


public class PrivacyPolicyFragment extends BaseFragment
{
    private static final String TAG = PrivacyPolicyFragment.class.getSimpleName();

    @Override
    protected String getScreenName()
    {
        return SCREEN.PRIVACY.getScreenName();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTopBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_privacy_policy, container, false);
        WebView webView = (WebView) view.findViewById(R.id.webview);
        String privacyUrl = Session.getInstance().config.privacyURL;

        webView.loadUrl(privacyUrl);

        return view;
    }

    @Override
    protected void updateTopBar()
    {
        Log.i(TAG, "updateTopBar");

        updateToolBarTitle(getScreenName());
    }
}
