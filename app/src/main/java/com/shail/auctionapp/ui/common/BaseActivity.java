package com.shail.auctionapp.ui.common;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ProgressBar;

import io.app..R;

/**
 * Created by iTexico Developer on 3/30/2016.
 */
public class BaseActivity extends AppCompatActivity implements Handler.Callback {

    private static final int PROGRESS_SHOW_MSG = 1000;
    private static final int PROGRESS_HIDE_MSG = PROGRESS_SHOW_MSG + 1;

    private Dialog mProgressDialog;
    private Handler mMessageHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessageHandler = new Handler(getMainLooper(), this);
        initLoadingView();
    }

    private void initLoadingView() {
        ProgressBar progressBar = new ProgressBar(this);
        int padding = getResources().getDimensionPixelOffset(R.dimen.dialog_default_padding);
        progressBar.setPadding(padding, padding, padding, padding);

        mProgressDialog = new Dialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(progressBar);
        int color = ContextCompat.getColor(this, R.color._loading_color);
        final ColorDrawable loadingBackgroundColor = new ColorDrawable(color);
        mProgressDialog.getWindow().setBackgroundDrawable(loadingBackgroundColor);
    }

    public Dialog getProgressDialog() {
        return mProgressDialog;
    }

    protected void progressShow(long delay) {
        mMessageHandler.sendEmptyMessageDelayed(PROGRESS_SHOW_MSG, delay);
    }

    protected void progressHide() {
        mMessageHandler.removeMessages(PROGRESS_SHOW_MSG);
        mMessageHandler.sendEmptyMessage(PROGRESS_HIDE_MSG);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case PROGRESS_SHOW_MSG:
                mProgressDialog.show();
                break;

            case PROGRESS_HIDE_MSG:
                mProgressDialog.dismiss();
                break;
        }
        return false;
    }

}
