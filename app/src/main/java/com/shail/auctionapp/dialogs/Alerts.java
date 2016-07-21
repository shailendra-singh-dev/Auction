package com.shail.auctionapp.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.shail.auctionapp.interfaces.AlertBlock;


public class Alerts
{
    public static void okAlert(Context context, String message, final AlertBlock block)
    {
        AlertDialog alert = new AlertDialog.Builder(context).create();
        alert.setTitle("Alert");
        alert.setMessage(message);
        alert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                        case DialogInterface.BUTTON_NEUTRAL:
                            if (block != null)
                                block.onOk();
                        break;
                    }

                    dialog.dismiss();
                }
            }
        );
        alert.show();
    }

    public static void okAlert(Context context, String title, String message, final AlertBlock block)
    {
        AlertDialog alert = new AlertDialog.Builder(context).create();
        alert.setTitle(title);
        alert.setMessage(message);

        alert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                        case DialogInterface.BUTTON_NEUTRAL:
                            if (block != null)
                                block.onOk();
                            break;
                    }

                    dialog.dismiss();
                }
            }
        );
        alert.show();
    }

    public static void YesNoAlert(Context context, String title, String message, final AlertBlock block)
    {
        DialogInterface.OnClickListener clickHandler =
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                        case DialogInterface.BUTTON_NEUTRAL:
                            if (block != null)
                                block.onOk();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            if (block != null)
                                block.onNo();
                            break;
                    }

                    dialog.dismiss();
                }
            }
        ;

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setPositiveButton("Yes", clickHandler);
        alert.setNegativeButton("No", clickHandler);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.show();
    }
}
