package com.example.finalproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

public class IsUserConnectedToInternetThread {
    private Context context;
    private Handler handler;
    private final int CHECK_INTERVAL = 5000; // Check every 5 seconds
    private boolean lastStatus = false;
    private InternetStatusListener listener;

    public interface InternetStatusListener {
        void onInternetStatusChanged(boolean isConnected);
    }

    public IsUserConnectedToInternetThread(Context context, InternetStatusListener listener) {
        this.context = context;
        this.listener = listener;
        this.handler = new Handler(Looper.getMainLooper());
    }

    private final Runnable internetCheckRunnable = new Runnable() {
        @Override
        public void run() {
            boolean isConnected = isInternetAvailable();
            if (isConnected != lastStatus) { // Notify only on change
                lastStatus = isConnected;
                listener.onInternetStatusChanged(isConnected);
            }
            handler.postDelayed(this, CHECK_INTERVAL);
        }
    };

    public void startChecking() {
        handler.post(internetCheckRunnable);
    }

    public void stopChecking() {
        handler.removeCallbacks(internetCheckRunnable);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return false;
    }
}
