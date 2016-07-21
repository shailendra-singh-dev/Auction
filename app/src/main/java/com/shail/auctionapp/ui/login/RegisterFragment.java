package com.shail.auctionapp.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.app..R;
import com.shail.auctionapp.apiclients.Session;
import com.shail.auctionapp.apiclients.UserClient;
import com.shail.auctionapp.dialogs.Alerts;
import com.shail.auctionapp.interfaces.AlertBlock;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.interfaces.RegisterDelegate;
import com.shail.auctionapp.models.SCREEN;
import com.shail.auctionapp.models.UserProfile;
import com.shail.auctionapp.models.UserRegistered;
import com.shail.auctionapp.ui.common.BaseFragment;

public class RegisterFragment extends BaseFragment implements View.OnClickListener
{
    private static final String TAG = LoginFragment.class.getSimpleName();

    private EditText editFirstName;
    private EditText editLastName;
    private EditText editEmail;
    private EditText editPassword;

    public RegisterDelegate delegate;

    public RegisterFragment()
    {
        // Required empty public constructor
    }

    @Override
    protected String getScreenName()
    {
        return SCREEN.REGISTER.getScreenName();
    }

    @Override
    protected void updateTopBar()
    {
        Log.i(TAG, "updateTopBar");
        updateToolBarTitle(getScreenName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        final LinearLayout registerFirstNameParent = (LinearLayout)view.findViewById(R.id.register_first_name_parent);
        editFirstName   = (EditText)registerFirstNameParent.findViewById(R.id.register_first_name);

        final LinearLayout registerLastNameParent = (LinearLayout)view.findViewById(R.id.register_last_name_parent);
        editLastName    = (EditText)registerLastNameParent.findViewById(R.id.register_last_name);

        final LinearLayout registerEmailParent = (LinearLayout)view.findViewById(R.id.register_email_parent);
        editEmail       = (EditText)registerEmailParent.findViewById(R.id.register_email);

        final LinearLayout registerPaswordParent = (LinearLayout)view.findViewById(R.id.register_password_parent);
        editPassword    = (EditText)registerPaswordParent.findViewById(R.id.register_password);
        editPassword.setOnEditorActionListener(
            new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        register();
                        return true;
                    }

                    return false;
                }
            }
        );

        Button register = (Button)view.findViewById(R.id.button_register);
        register.setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_register, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_register:
                register();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_register:
                register();
                break;
        }
    }

    public String getEmail()
    {
        return editEmail.getText().toString();
    }

    public String getPassword()
    {
        return editPassword.getText().toString();
    }

    public boolean valid()
    {
        String value = editEmail.getText().toString();

        if ((value.length() < 7) || !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches())
        {
            Alerts.okAlert(getActivity(), "Invalid Email", "Please check your email.", null);

            editEmail.requestFocus();

            return false;
        }

        value = editPassword.getText().toString();
        if (value.length() < 5)
        {
            Alerts.okAlert(getActivity(), "Invalid Password", "Please enter more than 5 characters.", null);

            editPassword.requestFocus();

            return false;
        }

        return true;
    }

    public void register()
    {
        if (!valid())
            return;

        String email    = editEmail.getText().toString();
        String password = editPassword.getText().toString();

        UserClient.register(email, password,
            new CallBlockOne<UserRegistered>()
            {
                @Override
                public void onSuccess(UserRegistered response)
                {
                    registered();
                }

                @Override
                public void onFailure(Throwable t)
                {
                    Alerts.okAlert(getActivity(), "Registration Error", t.getMessage(),
                        new AlertBlock()
                        {
                            @Override
                            public void onOk() { }
                        }
                    );
                }
            }
        );

        updateProfile();
    }

    public void updateProfile()
    {
        Session session     = Session.getInstance();
        UserProfile profile = session.userProfile;

        profile.firstName   = editFirstName.getText().toString();
        profile.lastName    = editLastName.getText().toString();
        profile.email       = editEmail.getText().toString();

        session.updateUserProfile();
    }

    public void registered()
    {
        if (delegate != null)
            delegate.RegisterSuccess(RegisterFragment.this);
        else
            getAttachedActivity().popFragment();
    }
}
