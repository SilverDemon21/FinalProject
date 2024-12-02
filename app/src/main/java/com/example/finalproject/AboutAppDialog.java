package com.example.finalproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AboutAppDialog {

    public static void showAboutDialog(Context context) {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Inflate a custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_about_app, null);

        // Set the dialog view
        builder.setView(dialogView);

        // Add an "OK" button to dismiss the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.create().show();
    }
}
