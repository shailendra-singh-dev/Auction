package com.shail.auctionapp.models;

public class Asset
{
    public String id;
    public String url;

    @Override
    public String toString() {
        return "[ID:" + id + ",url:" + url + "]";
    }
}
