package com.shail.auctionapp.apiclients;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shail.auctionapp.interfaces.CallBlockOne;
import com.shail.auctionapp.models.AuctionItem;
import com.shail.auctionapp.models.CreditCard;
import com.shail.auctionapp.models.CreditCardAddCardInfo;
import com.shail.auctionapp.models.CreditCardAddCardResult;
import com.shail.auctionapp.models.CreditCardBrand;
import com.shail.auctionapp.models.CreditCardKeyResult;
import com.shail.auctionapp.models.CreditCardNewCardInfo;
import com.shail.auctionapp.models.CreditCardNewCustomerInfo;
import com.shail.auctionapp.models.CreditCardNewCustomerResult;
import com.shail.auctionapp.models.CreditCardPurchaseInfo;
import com.shail.auctionapp.models.CreditCardPurchaseResult;
import com.shail.auctionapp.models.CreditCardUpdateError;
import com.shail.auctionapp.models.CreditCardUpdateInfo;
import com.shail.auctionapp.models.CreditCardUpdateResult;
import com.shail.auctionapp.security.SecurityHandler;
import com.shail.auctionapp.utils.AppConst;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class CreditCardClient extends BaseClient
{
    private static final String TAG = CreditCardClient.class.getSimpleName();
    private static String cardStoreFilename  = AppConst._STORAGE_FILE_PATH + AppConst._STORAGE_FILE_NAME;

    public interface CreditCardService
    {
        @POST("charges/publishKey")
        Call<CreditCardKeyResult> getPublishKey();

        @POST("charges/newCustomer")
        Call<CreditCardNewCustomerResult> newCustomer(@Body CreditCardNewCustomerInfo info);

        @POST("charges/addCardToCustomer")
        Call<CreditCardAddCardResult> addCardToCustomer(@Body CreditCardAddCardInfo info);

        @POST("charges/updateCustomerCard")
        Call<CreditCardUpdateResult> updateCard(@Body CreditCardUpdateInfo info);

        @POST("charges")
        Call<CreditCardPurchaseResult> chargeCard(@Body CreditCardPurchaseInfo info);
    }

    private static ArrayList<CreditCard> cardList;

    private CreditCardClient()
    {
    }

    public static List<CreditCard> getCardList()
    {
        if (cardList != null) // if already loaded, return the list
            return cardList;

        //Load card list.
        try {
            SecurityHandler crypto  = new SecurityHandler(SecurityHandler.PASSWORD);

            String cardData     = crypto.readFromFile(cardStoreFilename);
            Type collectionType = new TypeToken<Collection<CreditCard>>() { }.getType();

            cardList = new Gson().fromJson(cardData, collectionType);

            if (cardList == null) // failed to decrypt (should never happen)
                cardList = new ArrayList<CreditCard>();
        } catch(Exception ex) {
            ex.printStackTrace();

            cardList = new ArrayList<CreditCard>();
        }

        return cardList;
    }

    private static void saveCardList()
    {
        try {
            String json                 = new Gson().toJson(cardList);
            SecurityHandler crypto  = new SecurityHandler(SecurityHandler.PASSWORD);

            crypto.saveToFile(cardStoreFilename, json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static CreditCard getDefaultCard()
    {
        List<CreditCard> cards = getCardList();

        if (cards.size() == 0)
            return null;

        return cards.get(0);
    }

    public static void saveCard(CreditCard card)
    {
        List<CreditCard> cards = getCardList();

        cards.add(card);

        saveCardList();
    }

    public static void removeCard(CreditCard card)
    {
        List<CreditCard> cards = getCardList();

        cards.remove(card);

        saveCardList();
    }

    public static void getPublishKey()
    {
        CreditCardService           service = create(CreditCardService.class);
        Call<CreditCardKeyResult>   call    = service.getPublishKey();

        call.enqueue(
            new Callback<CreditCardKeyResult>()
            {
                @Override
                public void onResponse(Call<CreditCardKeyResult> call, Response<CreditCardKeyResult> response)
                {
                    CreditCardKeyResult key = response.body();

                    if (key == null)
                        return;

                    Session session = Session.getInstance();

                    session.chargePublishKey = key.publishKey;

                    session.saveSession();
                }

                @Override
                public void onFailure(Call<CreditCardKeyResult> call, Throwable t)
                {
                    //ignore for now (shouldn't ever fail)
                }
            }
        );
    }

    public static void createCard(final CreditCardNewCardInfo cardInfo, final CallBlockOne<CreditCard> block)
    {
        final Card scard = new Card(
            cardInfo.cardNumber,
            cardInfo.expiresMonth,
            cardInfo.expiresYear,
            cardInfo.cvv
        );

        try {
            Stripe stripe = new Stripe(Session.getInstance().chargePublishKey);

            stripe.createToken(scard,
                new TokenCallback()
                {
                    @Override
                    public void onSuccess(Token token)
                    {
                        CreditCard card         = new CreditCard();
                        card.customerId         = cardInfo.customerChargeId;
                        card.cardNumberMasked   = "---- ---- ---- " + token.getCard().getLast4();
                        card.cardToken          = token.getId();

                        switch(scard.getType())
                        {
                            case Card.AMERICAN_EXPRESS: card.cardType = CreditCardBrand.AMEX;       break;
                            case Card.DINERS_CLUB:      card.cardType = CreditCardBrand.DINERSCLUB; break;
                            case Card.DISCOVER:         card.cardType = CreditCardBrand.DISCOVER;   break;
                            case Card.JCB:              card.cardType = CreditCardBrand.JCB;        break;
                            case Card.MASTERCARD:       card.cardType = CreditCardBrand.MASTERCARD; break;
                            case Card.VISA:             card.cardType = CreditCardBrand.VISA;       break;
                            case Card.UNKNOWN:          card.cardType = CreditCardBrand.UNKNOWN;    break;
                            default:                    card.cardType = CreditCardBrand.UNKNOWN;    break;
                        }

                        block.onSuccess(card);
                    }

                    @Override
                    public void onError(Exception error)
                    {
                        block.onFailure(new Throwable(error.getLocalizedMessage()));
                    }
                }
            );
        }
        catch (AuthenticationException ex)
        {
            block.onFailure(new Throwable(ex.getMessage()));
        }
    }

    public static void createCustomerWithCard(String email, final CreditCard card, final CallBlockOne<CreditCard> block)
    {
        CreditCardService           service = create(CreditCardService.class);
        CreditCardNewCustomerInfo   info    = new CreditCardNewCustomerInfo();

        info.email          = email;
        info.cardTokenId    = card.cardToken;

        Call<CreditCardNewCustomerResult> call = service.newCustomer(info);

        call.enqueue(
            new Callback<CreditCardNewCustomerResult>()
            {
                @Override
                public void onResponse(Call<CreditCardNewCustomerResult> call, Response<CreditCardNewCustomerResult> response)
                {
                    CreditCardNewCustomerResult cust = response.body();

                    if (cust == null)
                    {
                        block.onFailure(new Throwable("Card Processing Error"));
                        return;
                    }

                    card.customerId = cust.customerId;
                    card.cardId     = cust.cardId;
                    card.cardToken  = null;

                    saveCard(card);

                    block.onSuccess(card);
                }

                @Override
                public void onFailure(Call<CreditCardNewCustomerResult> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void addCardToCustomer(final CreditCard card, final CallBlockOne<CreditCard> block)
    {
        CreditCardService service = create(CreditCardService.class);

        CreditCardAddCardInfo info = new CreditCardAddCardInfo();

        info.customerId     = card.customerId;
        info.cardTokenId    = card.cardToken;

        Call<CreditCardAddCardResult> call = service.addCardToCustomer(info);

        call.enqueue(
            new Callback<CreditCardAddCardResult>()
            {
                @Override
                public void onResponse(Call<CreditCardAddCardResult> call, Response<CreditCardAddCardResult> response)
                {
                    CreditCardAddCardResult added = response.body();

                    if (added == null)
                    {
                        block.onFailure(new Throwable("Card Processing Error."));
                        return;
                    }

                    if (added.cardId.length() == 0)
                    {
                        block.onFailure(new Throwable("Card was declined."));
                        return;
                    }

                    card.cardToken  = null;
                    card.cardId     = added.cardId;

                    saveCard(card);

                    block.onSuccess(card);
                }

                @Override
                public void onFailure(Call<CreditCardAddCardResult> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void chargeCard(CreditCard card, AuctionItem item, final CallBlockOne<CreditCardPurchaseResult> block)
    {
        CreditCardService service = create(CreditCardService.class);

        CreditCardPurchaseInfo info = new CreditCardPurchaseInfo();

        info.chargeType         = "stripe";
        info.chargeForType      = "auctionItem";
        info.chargeForId        = item.id;
        info.chargeCustomerId   = card.customerId;
        info.chargeCardId       = card.cardId;
        info.chargeDescription  = item.title;
        info.chargeAmount       = (int)(item.topBidAmount * 100.0f); // convert to pennies

        Call<CreditCardPurchaseResult> call = service.chargeCard(info);

        call.enqueue(
            new Callback<CreditCardPurchaseResult>()
            {
                @Override
                public void onResponse(Call<CreditCardPurchaseResult> call, Response<CreditCardPurchaseResult> response)
                {
                    CreditCardPurchaseResult result = response.body();

                    if (result == null)
                    {
                        block.onFailure(new Throwable("Unable to reach the auction house.  Please try again later."));
                        return;
                    }

                    if (result.resultCode.equals("card_charged"))
                    {
                        block.onSuccess(result);
                        return;
                    }

                    if (result.resultMessage.contains("idempotent")) // duplicate charge
                    {
                        block.onSuccess(result);
                        return;
                    }

                    // If we got here it's a total failure.
                    block.onFailure(new Throwable("Unable to process your card transaction."));
                }

                @Override
                public void onFailure(Call<CreditCardPurchaseResult> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }

    public static void updateCard(CreditCard card, int expiresMonth, int expiresYear, final CallBlockOne<CreditCardUpdateResult> block)
    {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Session.getInstance().ApiBaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CreditCardUpdateInfo info   = new CreditCardUpdateInfo();
        info.customerId             = card.customerId;
        info.cardId                 = card.cardId;
        info.expiresMonth           = expiresMonth;
        info.expiresYear            = expiresYear;

        CreditCardService service           = retrofit.create(CreditCardService.class);
        Call<CreditCardUpdateResult> call   = service.updateCard(info);

        call.enqueue(
            new Callback<CreditCardUpdateResult>()
            {
                @Override
                public void onResponse(Call<CreditCardUpdateResult> call, Response<CreditCardUpdateResult> response)
                {
                    if (response == null)
                    {
                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    CreditCardUpdateResult result = response.body();
                    if (result == null)
                    {
                        // some error has occured as part of the update process

                        /*
                            response for an update that the server just didn't like

                        */
                        Converter<ResponseBody, CreditCardUpdateError> converter = retrofit.responseBodyConverter(CreditCardUpdateError.class, new Annotation[0]);
                        try {
                            CreditCardUpdateError info = converter.convert(response.errorBody());

                            block.onFailure(new Throwable((info.error.message)));

                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                        return;
                    }

                    // need to add error handler...
                    block.onSuccess(result);
                }

                @Override
                public void onFailure(Call<CreditCardUpdateResult> call, Throwable t)
                {
                    block.onFailure(new Throwable("I'm sorry, we're not able to reach the auction house right now."));
                }
            }
        );
    }
}
