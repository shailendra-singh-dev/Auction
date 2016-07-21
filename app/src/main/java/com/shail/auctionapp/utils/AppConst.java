package com.shail.auctionapp.utils;

import com.shail.auctionapp.application.Application;

public class AppConst
{
    public static final double  AUCTIONS_SCREEN_ROW_ASPECT_RATIO = 0.5625;

    public static final String  _STORAGE_FILE_NAME = "credit_cards.txt";
    public static final String  _STORAGE_FILE_PATH = Application.getInstance().getFilesDir().getPath()+"/";

    public static final String  DEPRECATION = "deprecation";;

    public static final long    PROGRESS_SHOW_MSG_DELAY = 1000;
    public static final long    PROGRESS_SHOW_MSG_NOW   = 0;

    private AppConst()
    {
    }
}
