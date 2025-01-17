package com.example.finalproject.mainAplication;

import org.osmdroid.config.Configuration;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.tv.AdRequest;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject.MainActivity;
import com.example.finalproject.Permission;
import com.example.finalproject.R;
import com.example.finalproject.ShowAllUsers.UsersActivity;
import com.example.finalproject.sharedPref_manager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class mapAndLogic extends AppCompatActivity {

    private MapView mapView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker userMarker;
    private DatabaseReference databaseReference;
    private LocationCallback locationCallback;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_logic);

        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

        databaseReference = FirebaseDatabase.getInstance().getReference();

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getZoomController().setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        userMarker = new Marker(mapView);
        userMarker.setTitle("You are here!");
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(userMarker);

        if (checkPermissions()) {
           startLocationUpdates();
        } else {
            requestPermissions();
        }


        // adds an event listener for the map
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                double latitude = p.getLatitude();
                double longitude = p.getLongitude();

                String address = getAddressFromCoordinates(latitude, longitude);

                sharedPref_manager manager = new sharedPref_manager(mapAndLogic.this, "LoginUpdate");
                SavedLocation savedLocation = new SavedLocation();
                savedLocation.setAddress(address);
                savedLocation.setLatitude(latitude);
                savedLocation.setLongitude(longitude);
                savedLocation.setUsername(manager.getUsername());

                showConfirmationSavingLocation(mapAndLogic.this,savedLocation, p);


                return true;
            }
        };

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapView.getOverlays().add(eventsOverlay);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null) return;
                for(Location location : locationResult.getLocations()){
                    updateLocationOnMap(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        updateLocationOnMap(location);
                    }
                });
    }


    // <editor-fold desc="Menu items">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_button_go_back_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.menu_go_back){
            Intent intent = new Intent(mapAndLogic.this, MainActivity.class);
            startActivity(intent);
        }
        return true;
    }
    // </editor-fold>

    // <editor-fold desc="permission staff">
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }
    // </editor-fold>



    private void startLocationUpdates(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }

    }


    private void updateLocationOnMap(Location location){
        if(location != null){
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            GeoPoint userLocation = new GeoPoint(latitude, longitude);
            userMarker.setPosition(userLocation);
            mapView.getController().animateTo(userLocation);
            mapView.invalidate();

            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();

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


    public void saveLocation(SavedLocation location, GeoPoint p){


        String locationId = databaseReference.child("SavedLocations").push().getKey();

        if(locationId != null){
            databaseReference.child("SavedLocations").child(locationId).setValue(location).addOnSuccessListener(aVoid ->{
                Map<String,Object> updates = new HashMap<>();
                updates.put("SavedLocations/" + locationId,true);
                databaseReference.child("users").child(location.getUsername()).updateChildren(updates).addOnSuccessListener(aVoid1 -> {

                });
            });
        }
        Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();

        makeMarker(p, location.getAddress());
    }

    public void showConfirmationSavingLocation(Context context, SavedLocation location, GeoPoint p){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Add Location");
        builder.setMessage("Do you want to add this location?");

        builder.setPositiveButton("Yes", (dialog,which) -> {
            saveLocation(location, p);
        });
        builder.setNegativeButton("No", (dialog,which) -> dialog.dismiss());

        builder.show();
    }


    private void makeMarker(GeoPoint p, String address){
        Marker marker = new Marker(mapView);
        marker.setPosition(p);
        marker.setTitle(address);
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); // Resume map view

        if (checkPermissions()) { // Check if permissions are granted
            startLocationUpdates(); // Restart location tracking
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause(); // Pause map view
    }
}


