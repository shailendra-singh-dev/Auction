package com.shail.auctionapp.models;

public class UserBid
{
    public String       id;
    public AuctionItem  auctionItem;
    public Bid          bid;

    @Override
    public String toString() {
        return "[ID:" + id + "," + auctionItem + "]";
    }
}
