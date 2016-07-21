package com.shail.auctionapp.apiclients;

import java.util.List;

import com.shail.auctionapp.interfaces.CallBlockOne;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import com.shail.auctionapp.interfaces.CallBlock;
import com.shail.auctionapp.models.UserProfile;
import com.shail.auctionapp.models.UserBid;
import retrofit2.http.Query;

public class UserProfileClient extends BaseClient
{
    public interface UserProfileService
    {
        @GET("userprofiles/{userProfileId}")
        Call<UserProfile> FetchUserProfile(@Path("userProfileId") String userProfileId);

        @GET("userprofiles/{userProfileId}/userbids?filter[include]=auctionItem&filter[include]=bid&filter[order]=updatedAt%20DESC")
        Call<List<UserBid>> FetchBids(@Path("userProfileId") String userProfileId);

        @PUT("userprofiles")
        Call<UserProfile> CreateUserProfile();

        @PUT("userprofiles")
        Call<UserProfile> UpdateUserProfile(@Body UserProfile profile);

        @GET("userprofiles")
        Call<List<UserProfile>> FetchUserProfileByUserId(@Query("filter[where][userId]") String userId);
    }

    protected UserProfileClient()
    {
    }

    public static void fetchUserProfileById(String userProfileId, final CallBlockOne<UserProfile> block)
    {
        UserProfileService  service = create(UserProfileService.class);
        Call<UserProfile>   call    = service.FetchUserProfile(userProfileId);

        call.enqueue(
            new Callback<UserProfile>()
            {
                @Override
                public void onResponse(Call<UserProfile> call, Response<UserProfile> response)
                {
                    UserProfile profile = response.body();

                    if (profile == null)
                    {
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    block.onSuccess(profile);
                }

                @Override
                public void onFailure(Call<UserProfile> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void fetchUserProfileBidsWithId(String userProfileId, final CallBlock<UserBid> block)
    {
        UserProfileService  service = create(UserProfileService.class);
        Call<List<UserBid>> call    = service.FetchBids(userProfileId);

        call.enqueue(
            new Callback<List<UserBid>>()
            {
                @Override
                public void onResponse(Call<List<UserBid>> call, Response<List<UserBid>> response)
                {
                    List<UserBid> bids = response.body();

                    // Bad Gateway error
                    if (bids == null) {
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    block.onSuccess(bids);
                }

                @Override
                public void onFailure(Call<List<UserBid>> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void createUserProfile(final CallBlockOne<UserProfile> block)
    {
        UserProfileService  service = create(UserProfileService.class);
        Call<UserProfile>   call    = service.CreateUserProfile();

        call.enqueue(
            new Callback<UserProfile>()
            {
                @Override
                public void onResponse(Call<UserProfile> call, Response<UserProfile> response)
                {
                    UserProfile profile = response.body();

                    // Bad Gateway error
                    if (profile == null) {
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    block.onSuccess(profile);
                }

                @Override
                public void onFailure(Call<UserProfile> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void updateUserProfile(UserProfile profile)
    {
        updateUserProfile(profile, null);
    }

    public static void updateUserProfile(UserProfile profile, final CallBlockOne<UserProfile> block)
    {
        UserProfileService  service = create(UserProfileService.class);
        Call<UserProfile>   call    = service.UpdateUserProfile(profile);

        call.enqueue(
            new Callback<UserProfile>()
            {
                @Override
                public void onResponse(Call<UserProfile> call, Response<UserProfile> response)
                {
                    UserProfile profile = response.body();

                    if (profile == null)
                    {
                        if (block != null)
                            block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    if (block != null)
                        block.onSuccess(profile);
                }

                @Override
                public void onFailure(Call<UserProfile> call, Throwable t)
                {
                    if (block != null)
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void fetchUserProfileWithUserId(String userId, final CallBlockOne<UserProfile> block)
    {
        UserProfileService      service = create(UserProfileService.class);
        Call<List<UserProfile>> call    = service.FetchUserProfileByUserId(userId);

        call.enqueue(
            new Callback<List<UserProfile>>()
            {
                @Override
                public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response)
                {
                    List<UserProfile> profiles = response.body();

                    if (profiles == null)
                    {
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    UserProfile profile = (profiles.size() < 1) ? null : profiles.get(0);

                    block.onSuccess(profile);
                }

                @Override
                public void onFailure(Call<List<UserProfile>> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }
}
