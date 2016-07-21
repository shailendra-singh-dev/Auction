package com.shail.auctionapp.apiclients;

import java.util.List;

import com.shail.auctionapp.interfaces.CallBlock;
import com.shail.auctionapp.models.Auction;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;


public class AuctionsClient extends BaseClient
{
    public interface AuctionsService
    {
        @GET("auctions?filter[where][published]=true&filter[order][0]=auctionGroup&filter[order][1]=auctionStart&filter[include]=assets")
        Call<List<Auction>> auctions();
    }

    private AuctionsClient()
    {
    }

    public static void fetchAuctionList(final CallBlock<Auction> block)
    {
        AuctionsService     service = create(AuctionsService.class);
        Call<List<Auction>> call    = service.auctions();

        call.enqueue(
            new Callback<List<Auction>>()
            {
                @Override
                public void onResponse(Call<List<Auction>> call, Response<List<Auction>> response) {
                    List<Auction> auctions = response.body();

                    block.onSuccess(auctions);
                }

                @Override
                public void onFailure(Call<List<Auction>> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

}
