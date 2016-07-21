package com.shail.auctionapp.interfaces;

import java.util.List;

public interface CallBlock<T>
{
    void onSuccess(List<T> response);

    void onFailure(Throwable t);
}
