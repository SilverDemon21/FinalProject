package com.example.finalproject;

import android.animation.ObjectAnimator;
import android.widget.ImageView;

public class SplashAnimationThread extends Thread {

    private final SplashActivity activity;
    private final ImageView logo;

    public SplashAnimationThread(SplashActivity activity, ImageView logo) {
        this.activity = activity;
        this.logo = logo;
    }

    @Override
    public void run() {
        activity.runOnUiThread(() -> {
            ObjectAnimator rotation = ObjectAnimator.ofFloat(logo, "rotation", 0f, 360f);
            rotation.setDuration(2000);
            rotation.setRepeatCount(0);
            rotation.start();

            rotation.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    activity.goToMainActivity();
                }
            });
        });
    }
}