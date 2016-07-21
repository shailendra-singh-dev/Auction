package com.shail.auctionapp.ui.termsofuse;

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

public class TermsFragment extends BaseFragment {
    private static final String TAG = TermsFragment.class.getSimpleName();

    @Override
    protected String getScreenName() {
        return SCREEN.TERMS.getScreenName();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateTopBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terms, container, false);
        WebView webView = (WebView) view.findViewById(R.id.webview);
        String termsUrl = Session.getInstance().config.termsURL;

        webView.loadUrl(termsUrl);

        return view;
    }

    @Override
    protected void updateTopBar()
    {
        Log.i(TAG, "updateTopBar");
        updateToolBarTitle(getScreenName());
    }
}
