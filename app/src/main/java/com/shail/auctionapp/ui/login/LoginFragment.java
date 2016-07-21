package com.shail.auctionapp.ui.login;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.app..R;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.apiclients.UserClient;
import com.shail.auctionapp.apiclients.UserProfileClient;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.interfaces.LoginDelegate;
import com.shail.auctionapp.interfaces.RegisterDelegate;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.models.UserLogin;
import com.shail.auctionapp.models.UserProfile;
import com.shail.auctionapp.models.UserReset;
import com.shail.auctionapp.ui.common.BaseFragment;
import com.shail.auctionapp.utils.AppConst;

public class LoginFragment extends BaseFragment implements View.OnClickListener, RegisterDelegate, BaseFragment.IOnAlertDialogClickListener
{
    private static final String TAG = LoginFragment.class.getSimpleName();

    private TextView    editEmail;
    private TextView    editPassword;

    public LoginDelegate delegate;

    public LoginFragment()
    {
    }

    @Override
    protected String getScreenName() {
        return SCREEN.LOGIN.getScreenName();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateTopBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        final LinearLayout editEmailParent = (LinearLayout) view.findViewById(R.id.edit_email_parent);
        editEmail = (TextView)editEmailParent.findViewById(R.id.editEmail);

        Button login = (Button)view.findViewById(R.id.button_login);
        login.setOnClickListener(this);

        Button register = (Button)view.findViewById(R.id.button_register);
        register.setOnClickListener(this);

        final LinearLayout editPasswordParent = (LinearLayout) view.findViewById(R.id.edit_password_parent);
        editPassword = (TextView)editPasswordParent.findViewById(R.id.editPassword);
        editPassword.setTypeface(Typeface.DEFAULT);
        editPassword.setOnEditorActionListener(
            new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        login();
                        return true;
                    }

                    return false;
                }
            }
        );

        final TextView forgotPassword = (TextView)view.findViewById(R.id.forgot_password);
        forgotPassword.setText(Html.fromHtml(getString(R.string.forgot_password)));

        forgotPassword.setOnClickListener(this);
    }

    @Override
    protected void updateTopBar()
    {
        Log.i(TAG, "updateTopBar");

        updateToolBarTitle(getScreenName());
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_login:
                login();
                break;

            case R.id.button_register:
                register();
                break;

            case R.id.forgot_password:
                forgotPassword();
                break;
        }
    }

    public void forgotPassword()
    {
        showAlertDialogWithPositiveNegativeButton(R.string.dialog_forgot_password_postive_buttton_id,
                R.string.dialog_forgot_password_negative_buttton_id, R.string.dialog_forgot_password_title,
                getString(R.string.dialog_forgot_password_msg), this);
    }

    public boolean validEmail(String value)
    {
        if ((value.length() < 7) || !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches())
        {
            Alerts.okAlert(getActivity(), "Invalid Email", "The email address entered is invalid. A valid email address looks like 'johndoe@apple.com'.", null);

            return false;
        }

        return true;
    }

    public boolean validPassword(String value)
    {
        if (value.length() < 5) {
            Alerts.okAlert(getActivity(), "Invalid Password", "Please enter more than 5 characters.", null);
            return false;
        }

        return true;
    }

    public boolean valid()
    {
        if (!validEmail(editEmail.getText().toString()))
        {
            editEmail.requestFocus();
            return false;
        }

        if (!validPassword(editPassword.getText().toString()))
        {
            editPassword.requestFocus();

            return false;
        }

        String value = editPassword.getText().toString();

        return true;
    }

    public void login()
    {
        if (valid() == false)
            return;

        final UserProfile profile = Session.getInstance().userProfile;
        profile.email   = editEmail.getText().toString();
        String word     = editPassword.getText().toString();

        progressShow(AppConst.PROGRESS_SHOW_MSG_NOW);
        UserClient.login(profile.email, word,
            new CallBlockOne<UserLogin>()
            {
                @Override
                public void onSuccess(UserLogin login)
                {
                    progressHide();

                    loginSuccess(login);
                }

                @Override
                public void onFailure(Throwable t)
                {
                    progressHide();

                    Alerts.okAlert(getActivity(), "Login", "We do not recognize the information entered.  Please try again.", null);
                }
            }
        );
    }

    public void loginSuccess(final UserLogin login)
    {
        Session session         = Session.getInstance();
        session.sessionUserId   = login.userId;
        session.sessionKey      = login.id;

        progressShow(AppConst.PROGRESS_SHOW_MSG_NOW);
        UserProfileClient.fetchUserProfileWithUserId(login.userId,
            new CallBlockOne<UserProfile>()
            {
                @Override
                public void onSuccess(UserProfile response)
                {
                    progressHide();

                    Session session = Session.getInstance();
                    UserProfile profile = response;

                    if (profile == null) {
                        profile = session.userProfile;
                        profile.userId = login.userId;

                        session.updateUserProfile();
                    }

                    session.setupSession(login, profile);

                    if (delegate != null)
                        delegate.LoginSuccess(LoginFragment.this);
                }

                @Override
                public void onFailure(Throwable t)
                {
                    progressHide();

                    Alerts.okAlert(getActivity(), "Profile Error", null);
                }
            }
        );
    }

    public void register()
    {
        RegisterFragment register = new RegisterFragment();

        register.delegate = this;

        getAttachedActivity().pushFragment(register);
    }

    public void RegisterSuccess(RegisterFragment registerFragment)
    {
        editEmail.setText(registerFragment.getEmail());
        editPassword.setText(registerFragment.getPassword());

        login();

        getAttachedActivity().popFragment();
    }

    @Override
    public void onAlertDialogPositiveButtonClicked(String email)
    {
        if (!validEmail(email))
            return;

        progressShow(AppConst.PROGRESS_SHOW_MSG_NOW);
        UserClient.resetPassword(email,
            new CallBlockOne<UserReset>()
            {
                @Override
                public void onSuccess(UserReset response)
                {
                    progressHide();

                    showAlertDialogWithPositiveButton(R.string.dialog_success_postive_buttton_id, R.string.dialog_success_title, getString(R.string.dialog_success_msg));
                }

                @Override
                public void onFailure(Throwable t)
                {
                    progressHide();

                    String message= t.getMessage();

                    showAlertDialogWithPositiveButton(R.string.dialog_failure_postive_buttton_id, R.string.dialog_failure_title, message);
                }
            }
        );
    }

}
