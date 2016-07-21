package com.shail.auctionapp.models;

public class CreditCardUpdateError
{
    /*
        {
            [Error: Your card's expiration year is invalid.]
            type: 'StripeCardError',
            rawType: 'card_error',
            code: 'invalid_expiry_year',
            param: 'exp_year',
            message: 'Your card\'s expiration year is invalid.',
            detail: undefined,
            raw: {
                message: 'Your card\'s expiration year is invalid.',
                type: 'card_error',
                param: 'exp_year',
                code: 'invalid_expiry_year',
                statusCode: 402,
                requestId: 'req_84UZuggqfvvoKU'
            },
            requestId: 'req_84UZuggqfvvoKU',
            statusCode: 402
        }
    */
    public class CreditCardUpdateErrorDetail
    {
        public String   type;
        public String   rawType;
        public String   code;
        public String   param;
        public String   message;
        public String   requestId;
        public int      statusCode;
    }

    public CreditCardUpdateErrorDetail error;
}
