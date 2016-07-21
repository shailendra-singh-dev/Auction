package com.shail.auctionapp.apiclients;

import com.shail.auctionapp.interfaces.CallBlock;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.models.AuctionBidErrorInfo;
import com.shail.auctionapp.models.AuctionItem;
import com.shail.auctionapp.models.AuctionItemBidInfo;
import com.shail.auctionapp.models.CreditCard;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


public class AuctionItemsClient extends BaseClient
{
    public interface AuctionItemService
    {
        @GET("auctions/{auctionId}/items?filter[where][published]=true&filter[order]=sequence&filter[include]=assets")
        Call<List<AuctionItem>> auctionItems(@Path("auctionId") String auctionId);

        @GET("auctionitems/{auctionItemId}?filter[include]=assets")
        Call<AuctionItem> auctionItem(@Path("auctionItemId") String auctionItemId);

        @POST("auctionitems/newautopaybid")
        Call<AuctionItem> bidOnAuctionItem(@Body AuctionItemBidInfo bidInfo);
    }

    private AuctionItemsClient()
    {
    }

    public static void fetchAuctionItems(String auctionId, final CallBlock<AuctionItem> block)
    {
        AuctionItemService      service = create(AuctionItemService.class);
        Call<List<AuctionItem>> call    = service.auctionItems(auctionId);

        call.enqueue(
            new Callback<List<AuctionItem>>()
            {
                @Override
                public void onResponse(Call<List<AuctionItem>> call, Response<List<AuctionItem>> response)
                {
                    List<AuctionItem> auctionItems = response.body();

                    if (auctionItems == null) {
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    block.onSuccess(auctionItems);
                }

                @Override
                public void onFailure(Call<List<AuctionItem>> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void fetchAuctionItemById(String auctionItemId, final CallBlockOne<AuctionItem> block)
    {
        AuctionItemService  service = create(AuctionItemService.class);
        Call<AuctionItem>   call    = service.auctionItem(auctionItemId);

        call.enqueue(
            new Callback<AuctionItem>()
            {
                @Override
                public void onResponse(Call<AuctionItem> call, Response<AuctionItem> response)
                {
                    AuctionItem auctionItem = response.body();

                    if (auctionItem == null) {
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    block.onSuccess(auctionItem);
                }

                @Override
                public void onFailure(Call<AuctionItem> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void bidOnAuctionItem(AuctionItem item, float bidAmount, final CallBlockOne<AuctionItem> block)
    {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Session.getInstance().ApiBaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CreditCard card             = CreditCardClient.getDefaultCard();

        AuctionItemBidInfo bidInfo  = new AuctionItemBidInfo();
        bidInfo.bidAmount           = bidAmount;
        bidInfo.forAuctionItemId    = item.id;
        bidInfo.byUserProfileId     = Session.getInstance().userProfileId;
        bidInfo.payWithCustomerId   = card.customerId;
        bidInfo.payWithCardId       = card.cardId;

        AuctionItemService  service = retrofit.create(AuctionItemService.class);
        Call<AuctionItem>   call    = service.bidOnAuctionItem(bidInfo);

        call.enqueue(
            new Callback<AuctionItem>()
            {
                @Override
                public void onResponse(Call<AuctionItem> call, Response<AuctionItem> response)
                {
                    if (response == null)
                    {
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    AuctionItem item = response.body();
                    if (item == null)
                    {
                        // some error has occured as part of the bidding process

                        /*
                            response for a bid that's lower than the minimum

                            HTTP/1.1 500 Internal Server Error
                            Server: nginx/1.4.6 (Ubuntu)
                            Date: Thu, 17 Mar 2016 17:51:24 GMT
                            Content-Type: application/json; charset=utf-8
                            Content-Length: 89
                            Connection: keep-alive
                            X-Powered-By: Express
                            Vary: Origin, Accept-Encoding
                            Access-Control-Allow-Credentials: true
                            Access-Control-Expose-Headers: Content-Range,X-Content-Range
                            ETag: W/"59-DXeUPyItmbdesmMogijMdg"

                            {"error":{"name":"Error","status":500,"message":"You've been outbid.","topBidAmount":28}}
                        */
                        Converter<ResponseBody, AuctionBidErrorInfo> converter = retrofit.responseBodyConverter(AuctionBidErrorInfo.class, new Annotation[0]);
                        try {
                            AuctionBidErrorInfo info = converter.convert(response.errorBody());

                            block.onFailure(new Throwable((info.error.message)));

                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    block.onSuccess(item);
                }

                @Override
                public void onFailure(Call<AuctionItem> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }
}
