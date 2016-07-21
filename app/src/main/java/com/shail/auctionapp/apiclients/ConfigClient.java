package com.shail.auctionapp.apiclients;

import java.util.List;

import com.shail.auctionapp.interfaces.CallBlock;
import com.shail.auctionapp.models.Config;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public final class ConfigClient  extends BaseClient
{
    public interface ConfigService
    {
        @GET("configs")
        Call<List<Config>> configs(@Query("filter[where][name]") String name);
    }

    public ConfigClient()
    {
    }

    public static void fetchConfigDetails(String configName, final CallBlock<Config> block)
    {
        ConfigService       service = create(ConfigService.class, "https://config.app.io/api/");
        Call<List<Config>>  call    = service.configs(configName);

        call.enqueue(
            new Callback<List<Config>>()
            {
                @Override
                public void onResponse(Call<List<Config>> call, Response<List<Config>> response)
                {
                    List<Config> configs = response.body();

                    // Bad Gateway error
                    if (configs == null) {
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    // Bad configuration error
                    if (configs.size() < 1) {
                        block.onFailure(new Throwable("I'm sorry, the auction house is temporarily closed."));
                        return;
                    }

                    block.onSuccess(configs);
                }

                @Override
                public void onFailure(Call<List<Config>> call, Throwable t)
                {
                    //Connection failure (connection refused)
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }
}
