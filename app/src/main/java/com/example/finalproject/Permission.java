package com.example.finalproject;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permission {

    public static boolean DoesHavePrem(Activity activity) {
        int resultCamera = ContextCompat.checkSelfPermission(activity, CAMERA);
        int resultWriteStorage = ContextCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE);
        int resultReadStorage = ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE);
        int resultFineLocation = ContextCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION);
        int resultCoarseLocation = ContextCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ only checks CAMERA and LOCATION
            return resultCamera == PackageManager.PERMISSION_GRANTED;

        }

        // Android below 10 checks all permissions
        return resultCamera == PackageManager.PERMISSION_GRANTED &&
                resultWriteStorage == PackageManager.PERMISSION_GRANTED &&
                resultReadStorage == PackageManager.PERMISSION_GRANTED;

    }

    // Request permissions
    public static void GrantPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Request CAMERA and LOCATION permissions for Android 10+
            ActivityCompat.requestPermissions(activity, new String[]{
                    CAMERA}, 1);
        } else {
            // Request CAMERA, STORAGE, and LOCATION permissions for older versions
            ActivityCompat.requestPermissions(activity, new String[]{
                    CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 1);
        }
    }
}
