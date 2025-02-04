package com.example.finalproject.mainAplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.finalproject.sharedPref_manager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback serviceLocationCallback;
    private LocationRequest locationRequest;
    private DatabaseReference database;


    @Override
    public void onCreate(){
        super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create()
                .setInterval(1000 * 11)
                .setFastestInterval(1000 * 6)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        serviceLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null) return;
                for(Location location : locationResult.getLocations()){
                    updateLocationOnFirebase(location);
                }
            }
        };
    }

    private void updateLocationOnFirebase(Location location){
        sharedPref_manager manager = new sharedPref_manager(LocationService.this, "LoginUpdate");
        database = FirebaseDatabase.getInstance().getReference("users").child(manager.getUsername()).child("UserLocation");

        if(location != null){
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            String address = getAddressFromCoordinates(latitude, longitude);


            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", String.valueOf(latitude));
            locationData.put("longitude", String.valueOf(longitude));
            locationData.put("address", address);

            database.setValue(locationData)
                    .addOnSuccessListener(aVoid -> Log.d("LocationService", "Location updated for user: " + manager.getUsername()))
                    .addOnFailureListener(e -> Log.w("LocationService", "Failed to update location for user", e));
        }
    }

    private String getAddressFromCoordinates(double latitude, double longitude){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(addresses != null && !addresses.isEmpty()){
                Address address = addresses.get(0);
                return  address.getAddressLine(0);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "Unknown address";
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        startForegroundServiceWithNotification();
        startServiceLocationUpdates();
        return START_STICKY;
    }

    private void startForegroundServiceWithNotification(){
        Notification notification = new NotificationCompat.Builder(this, "location_channel")
                .setContentTitle("Location Tracking")
                .setContentText("Tracking your location in the background")
                .build();

        startForeground(1, notification);
    }

    private void startServiceLocationUpdates(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
           return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, serviceLocationCallback, Looper.getMainLooper());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(fusedLocationProviderClient != null){
            fusedLocationProviderClient.removeLocationUpdates(serviceLocationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }





}
