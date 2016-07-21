package com.shail.auctionapp.interfaces;

public interface CallBlockOne<T>
{
    void onSuccess(T response);

    void onFailure(Throwable t);
}