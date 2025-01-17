package com.example.finalproject.mainAplication;

import org.osmdroid.config.Configuration;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.tv.AdRequest;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.google.firebase.database.DataSnapshot;
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

        sharedPref_manager manager = new sharedPref_manager(mapAndLogic.this, "LoginUpdate");

        databaseReference = FirebaseDatabase.getInstance().getReference();

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getZoomController().setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);



        userMarker = new Marker(mapView);
        userMarker.setTitle("You are here!");
        String userUrl = manager.getPhotoUrl();
        Glide.with(this)
                .asBitmap()
                .load(userUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Resize the image
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(resource, 80, 80, false);
                        Bitmap circularBitmap = getCircularBitmap(resizedBitmap);

                        // Set the resized image as the marker icon
                        userMarker.setIcon(new BitmapDrawable(getResources(), circularBitmap));
                        mapView.invalidate(); // Refresh the map
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle placeholder if needed
                    }
                });

        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(userMarker);


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    updateLocationOnMap(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        updateLocationOnMap(location);
                        });
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


        if (checkPermissions()) {
            startLocationUpdates();
        } else {
            requestPermissions();
        }

        showAllSavedLocations();
    }

    // <editor-fold desc="Circular image setup">
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);

        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        return output;
    }
    // </editor-fold>

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
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Location permission is needed to show your location on the map.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                101);
    }
    // </editor-fold>



    private void startLocationUpdates(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * 11);
        locationRequest.setFastestInterval(1000 * 6);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



       if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
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

            Toast.makeText(this, "new location", Toast.LENGTH_SHORT).show();

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



    private void showAllSavedLocations(){
        sharedPref_manager manager = new sharedPref_manager(mapAndLogic.this, "LoginUpdate");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(manager.getUsername()).child("SavedLocations");


        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot locationSnapshot : task.getResult().getChildren()) {
                    String locationId = locationSnapshot.getValue(String.class);
                    Log.e("fds",locationId);

                    // Fetch location details using the locationId
                    DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("SavedLocations").child(locationId);

                    locationRef.get().addOnCompleteListener(locationTask -> {
                        if (locationTask.isSuccessful() && locationTask.getResult().exists()) {
                            // Parse location data
                            Log.e("fds",locationId);
                            double latitude = locationTask.getResult().child("latitude").getValue(Double.class);
                            double longitude = locationTask.getResult().child("longitude").getValue(Double.class);
                            String address = locationTask.getResult().child("address").getValue(String.class);

                            // Add marker to the map
                            GeoPoint point = new GeoPoint(latitude, longitude);
                            makeMarker(point, address);
                        }
                    });
                }
            } else {
                Toast.makeText(this, "No saved locations found", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permission denied! Cannot access location.", Toast.LENGTH_SHORT).show();
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


