package com.example.finalproject;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class activity_splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView splashLogo = findViewById(R.id.splash_logo);

        ObjectAnimator rotation = ObjectAnimator.ofFloat(splashLogo, "rotation", 0f, 360f);
        rotation.setDuration(2000);
        rotation.setRepeatCount(0);
        rotation.start();

        rotation.addListener(new android.animation.AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(android.animation.Animator animation){
                Intent intent = new Intent(activity_splash.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }
}