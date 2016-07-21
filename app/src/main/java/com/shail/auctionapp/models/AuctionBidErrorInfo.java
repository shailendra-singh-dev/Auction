package com.shail.auctionapp.models;

public class AuctionBidErrorInfo
{
    public class AuctionBidErrorDetail
    {
        public String name;
        public String status;
        public String message;
        public String topBidAmount;
        public String topBidUserProfileId;
    }

    public AuctionBidErrorDetail error;
}

