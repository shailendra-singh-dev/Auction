package com.shail.auctionapp.apiclients;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseClient
{
    protected static Retrofit getRetrofit()
    {
        Session session = Session.getInstance();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(session.ApiBaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                ;

        final String sessionKey = session.sessionKey;
        if ((sessionKey != null) && (sessionKey.length() > 0))
        {
            OkHttpClient authHttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                    new Interceptor()
                    {
                        @Override
                        public Response intercept(Chain chain) throws IOException
                        {
                            Request request = chain.request().newBuilder()
                                    .addHeader("Authorization", sessionKey)
                                    .build();

                            return chain.proceed(request);
                        }
                    }
                )
                .build();

            builder.client(authHttpClient);
        }

        return builder.build();
    }

    protected static <T> T create(Class<T> service)
    {
        Retrofit retrofit = getRetrofit();

        return retrofit.create(service);
    }

    protected static <T> T create(Class<T> service, String apiBaseURL)
    {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(apiBaseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        ;

        return retrofit.create(service);
    }
}
