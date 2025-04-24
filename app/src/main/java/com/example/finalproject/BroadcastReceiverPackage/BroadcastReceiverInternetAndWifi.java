package com.example.finalproject.BroadcastReceiverPackage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.widget.TextView;

import com.example.finalproject.R;

public class BroadcastReceiverInternetAndWifi extends BroadcastReceiver {

    private TextView statusTextView;

    public BroadcastReceiverInternetAndWifi(TextView textView){
        statusTextView = textView;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
                    intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                updateTextView(context);
            }
        }
    }

    private void updateTextView(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        boolean isConnected = false;
        boolean hasWiFi = wifiManager.isWifiEnabled();

        if (cm != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                isConnected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isConnected = activeNetwork != null && activeNetwork.isConnected();
            }
        }

        if (!isConnected && !hasWiFi) {
            statusTextView.setText("Disconnected");
            statusTextView.setBackgroundResource(R.drawable.background_color_person_disconnected);;
        } else {
            statusTextView.setText("Connected");
            statusTextView.setBackgroundResource(R.drawable.background_color_person_connected);
        }
    }
}