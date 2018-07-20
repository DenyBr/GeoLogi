package com.deny.GeoLogi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * Created by bruzlzde on 26.2.2018.
 */

public class Okynka {
    public static void zobrazOkynko(Context context, String text) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(text);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }




}
