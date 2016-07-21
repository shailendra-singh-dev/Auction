package com.shail.auctionapp.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.shail.auctionapp.models.SCREEN;

public abstract class BaseFragment extends Fragment {

    public interface IOnAlertDialogClickListener {
        void onAlertDialogPositiveButtonClicked(final String textInput);
    }

    public interface IOnDataAvailableListener{
        void onDataAvailableAtIndex(int index);
        void onDataAvailable();
        void onDataRemovedAtIndex(int index);
    }

    private IOnDataAvailableListener mIOnDataAvailableListener;

    private Activity mActivity;

    private IOnAlertDialogClickListener mIOnAlertDialogClickListener;

    public void setIOnDataAvailableListener(IOnDataAvailableListener onDataAvailableListener) {
        mIOnDataAvailableListener = onDataAvailableListener;
    }

    protected IOnDataAvailableListener getIOnDataAvailableListener() {
        return mIOnDataAvailableListener;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        mActivity = (Activity) context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        updateTopBarBackArrow();
    }

    public void OnAppResume()
    {
    }

    protected void updateToolBarTitle(final String title)
    {
        mActivity.setToolBarTitle(title);
    }

    private void updateTopBarBackArrow()
    {
        if (getScreenName().equalsIgnoreCase(SCREEN.AUCTIONS.getScreenName())) {
            mActivity.enableTopBarBackArrow(false);
        } else {
            mActivity.enableTopBarBackArrow(true);
        }
    }

    protected Activity getAttachedActivity()
    {
        return mActivity;
    }

    abstract protected String getScreenName();

    abstract protected void updateTopBar();

    protected void setIOnAlertDialogClickListener(IOnAlertDialogClickListener iOnAlertDialogClickListener)
    {
        mIOnAlertDialogClickListener = iOnAlertDialogClickListener;
    }

    protected void showAlertDialogWithPositiveNegativeButton(int positiveButtonTitleId, int negativeButtonTitleId, int titleId, String message, IOnAlertDialogClickListener iOnAlertDialogClickListener)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        setIOnAlertDialogClickListener(iOnAlertDialogClickListener);
        alertDialogBuilder.setTitle(titleId);
        final EditText input = new EditText(getAttachedActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setView(input);
        alertDialogBuilder.setPositiveButton(getString(positiveButtonTitleId), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String userInputText = input.getText().toString();
                if (null != mIOnAlertDialogClickListener) {
                    mIOnAlertDialogClickListener.onAlertDialogPositiveButtonClicked(userInputText);
                }
            }
        });
        alertDialogBuilder.setNegativeButton(getString(negativeButtonTitleId), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    protected void showAlertDialogWithPositiveButton(int positiveButtonTitleId, int titleId, String message)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder.setTitle(titleId);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(getString(positiveButtonTitleId), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    public Dialog getProgressDialog() {
        return mActivity.getProgressDialog();
    }

    protected void progressShow(long delay) {
        mActivity.progressShow(delay);
    }

    protected void progressHide() {
        mActivity.progressHide();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        progressHide();
    }
}
