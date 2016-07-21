package com.shail.auctionapp.ui.common;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import io.app..R;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.ui.auctions.AuctionsFragment;

public class Activity extends BaseActivity
{
    private static final String TAG = Activity.class.getSimpleName();
    private Toolbar mToolBar;

    private boolean launched = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initToolBar();
    }

    private void initToolBar()
    {
        mToolBar = (Toolbar) findViewById(R.id._tool_bar);
        mToolBar.setTitle("");

        setSupportActionBar(mToolBar);
    }

    public void enableTopBarBackArrow(final boolean isEnable)
    {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(isEnable);
        actionBar.setDisplayHomeAsUpEnabled(isEnable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //Handles ToolBar backArrow Navigation...
        if (item.getItemId() == android.R.id.home) {
            popFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setToolBarTitle(final String title)
    {
        mToolBar.setTitle(title);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        // My preference would be do do this on the application object, but Android doesn't
        // seem to work this way.  We need to make sure if we create new Activities that we
        // add this to those.  Maybe even create a app method that's called by all activities
        // onPause()?

        // Save session when we background
        Session.getInstance().saveSession();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // recover session on foregound
        Session.getInstance().recoverSession();
        Session.getInstance().messageManager.setActivity(this);

        if (launched == false)
        {
            launchAuctionsScreen();
            launched = true;
        }

        OnAppResume();
    }

    private void OnAppResume()
    {
        BaseFragment topFragment = getTopFragment();
        Log.d("fred", "topFragment is " + topFragment.getScreenName());

        topFragment.OnAppResume();
    }

    private void launchAuctionsScreen()
    {
        AuctionsFragment auctionsFragment = new AuctionsFragment();
        pushFragment(auctionsFragment);
    }

    public Toolbar getToolBar()
    {
        return mToolBar;
    }

    public void pushFragment(BaseFragment fragment)
    {
        dismissKeyboard();

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);

        fragmentTransaction
                .replace(R.id.main_panel, fragment, fragment.getScreenName())
                .addToBackStack(fragment.getScreenName())
                .commit();
        fragmentManager.executePendingTransactions();
    }

    public void dismissKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void popFragment()
    {
        dismissKeyboard();

        onBackPressed();
    }

    private BaseFragment getTopFragment()
    {
        FragmentManager fragmentManager   = getSupportFragmentManager();
        int stackCount                    = fragmentManager.getBackStackEntryCount();

        if (0 == stackCount)
            return null;

        FragmentManager.BackStackEntry backEntryFragment = fragmentManager.getBackStackEntryAt(stackCount - 1);

        Fragment fragment = fragmentManager.findFragmentByTag(backEntryFragment.getName());

        return (BaseFragment)fragment;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final int stackCount = fragmentManager.getBackStackEntryCount();

        if (0 == stackCount) {
            finish();
        }

        if (stackCount > 0) {
            final FragmentManager.BackStackEntry backEntryFragment = fragmentManager
                    .getBackStackEntryAt(stackCount - 1);
            final String fragmentTag = backEntryFragment.getName();
            Log.i(TAG, "onBackPressed(),fragmentTag:" + fragmentTag);
            final Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);

            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment);
        }
    }
}
