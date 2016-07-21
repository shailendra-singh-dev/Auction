package com.shail.auctionapp.apiclients;

import java.io.IOException;
import java.lang.annotation.Annotation;

import com.shail.auctionapp.models.UserAuth;
import com.shail.auctionapp.models.UserRegister;
import com.shail.auctionapp.models.UserRegisterErrorInfo;
import com.shail.auctionapp.models.UserRegistered;
import com.shail.auctionapp.models.UserReset;
import com.shail.auctionapp.models.UserResetInfo;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.models.UserLogin;


public class UserClient extends BaseClient
{
    public interface UserLoginService
    {
        @POST("userslogin")
        Call<UserRegistered> register(@Body UserRegister register);

        @POST("userslogin/login")
        Call<UserLogin> login(@Body UserAuth auth);

        @POST("userslogin/resetPassword")
        Call<UserReset> resetPassword(@Body UserResetInfo resetInfo);
    }

    private UserClient()
    {
    }

    public static void register(String user, String password, final CallBlockOne<UserRegistered> block)
    {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Session.getInstance().ApiBaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserRegister register   = new UserRegister();
        register.email          = user;
        register.password       = password;

        UserLoginService  service           = retrofit.create(UserLoginService.class);
        Call<UserRegistered> registerCall   = service.register(register);

        registerCall.enqueue(
            new Callback<UserRegistered>()
            {
                @Override
                public void onResponse(Call<UserRegistered> call, Response<UserRegistered> response)
                {
                    UserRegistered newUser = response.body();

                    if (newUser == null)
                    {
                        Converter<ResponseBody, UserRegisterErrorInfo> converter = retrofit.responseBodyConverter(UserRegisterErrorInfo.class, new Annotation[0]);
                        try {
                            UserRegisterErrorInfo info = converter.convert(response.errorBody());

                            block.onFailure(new Throwable((info.error.details.messages.email.get(0))));

                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    block.onSuccess(newUser);
                }

                @Override
                public void onFailure(Call<UserRegistered> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void login(String email, String password, final CallBlockOne<UserLogin> block)
    {
        UserLoginService service = create(UserLoginService.class);

        UserAuth auth   = new UserAuth();
        auth.email      = email;
        auth.password   = password;

        Call<UserLogin> userLoginCall = service.login(auth);

        userLoginCall.enqueue(
            new Callback<UserLogin>()
            {
                @Override
                public void onResponse(Call<UserLogin> call, Response<UserLogin> response)
                {
                    UserLogin login = response.body();

                    if (login == null)
                    {
                        block.onFailure(new Throwable("I'm sorry, we're not able to authenticate you."));
                        return;
                    }

                    block.onSuccess(login);
                }

                @Override
                public void onFailure(Call<UserLogin> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void resetPassword(String email, final CallBlockOne<UserReset> block)
    {
        UserLoginService service = create(UserLoginService.class);

        UserResetInfo info  = new UserResetInfo();
        info.email          = email;

        Call<UserReset> call = service.resetPassword(info);

        call.enqueue(
            new Callback<UserReset>()
            {
                @Override
                public void onResponse(Call<UserReset> call, Response<UserReset> response)
                {
                    UserReset reset = response.body();

                    if (reset == null)
                    {
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    if (reset.result.equals("success"))
                    {
                        block.onSuccess(reset);
                    }
                    else
                    {
                        block.onFailure(new Throwable(reset.message));
                    }
                }

                @Override
                public void onFailure(Call<UserReset> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }
}
