package com.example.finalproject;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permission {

    public static boolean DoesHavePrem(Activity activity){
        int resultCamera = ContextCompat.checkSelfPermission(activity, CAMERA);
        int resultWriteStorage = ContextCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE);
        int resultReadStorage = ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return resultCamera == PackageManager.PERMISSION_GRANTED;
        }


        return  resultCamera== PackageManager.PERMISSION_GRANTED &&
                resultWriteStorage==PackageManager.PERMISSION_GRANTED &&
                resultReadStorage==PackageManager.PERMISSION_GRANTED;

    }

    public static void GrantPermission(Activity activity){
        // For Android 10 and above, only request CAMERA permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, 1);

        }
        else{
            ActivityCompat.requestPermissions(activity, new String[]
                    {CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 1);
        }

    }
}
