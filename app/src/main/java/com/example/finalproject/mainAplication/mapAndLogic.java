package com.example.finalproject.mainAplication;

import org.osmdroid.config.Configuration;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.User_Profile;
import com.example.finalproject.sharedPref_manager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private Button btnShowSavedLocationsList;



    
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

        btnShowSavedLocationsList = findViewById(R.id.btnShowSavedLocations);

        sharedPref_manager manager = new sharedPref_manager(mapAndLogic.this, "LoginUpdate");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBarMap);

        bottomNavigationView.setSelectedItemId(R.id.menu_map);

        bottomNavigationView.setOnItemSelectedListener(item ->{
            if(item.getItemId() == R.id.menu_home){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if(item.getItemId() == R.id.menu_map){
                return true;
            }
            else if(item.getItemId() == R.id.menu_profile){
                startActivity(new Intent(getApplicationContext(), User_Profile.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;


        });

        btnShowSavedLocationsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mapAndLogic.this, listOfSavedLocations.class);
                startActivity(intent);
            }
        });



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
                savedLocation.setId(databaseReference.child("SavedLocations").push().getKey());
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
        getMenuInflater().inflate(R.menu.menu_add_savedlocations_ways, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.menu_makeLocationViaCoordinates){

            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.dialog_make_saved_location_via_coordinates, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            EditText etLatitudeForSaving = dialogView.findViewById(R.id.etLatitudeForSaving);
            EditText etLongitudeForSaving = dialogView.findViewById(R.id.etLongitudeForSaving);
            Button btnCreateSavedLocationViaCoordinates = dialogView.findViewById(R.id.btnCreateSavedLocationViaCoordinates);



            btnCreateSavedLocationViaCoordinates.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String latitude = etLatitudeForSaving.getText().toString().trim();
                    String longitude = etLongitudeForSaving.getText().toString().trim();
                    boolean correctCoordinates = validateCoordinates(latitude, longitude);

                    if (!correctCoordinates){
                        Toast.makeText(mapAndLogic.this, "Pls enter valid coordinates", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                    else {
                        sharedPref_manager manager = new sharedPref_manager(mapAndLogic.this, "LoginUpdate");
                        Toast.makeText(mapAndLogic.this, "goog", Toast.LENGTH_SHORT).show();
                        SavedLocation location = new SavedLocation();
                        location.setLongitude(Double.parseDouble(longitude));
                        location.setLatitude(Double.parseDouble(latitude));
                        location.setUsername(manager.getUsername());
                        location.setId(databaseReference.child("SavedLocations").push().getKey());
                        location.setAddress(getAddressFromCoordinates(Double.parseDouble(latitude), Double.parseDouble(longitude)));

                        GeoPoint p = new GeoPoint(Double.parseDouble(latitude), Double.parseDouble(longitude));

                        saveLocation(location, p);

                        dialog.dismiss();
                    }
                }
            });

            dialog.show();
        }
        if(item.getItemId() == R.id.menu_makeLocationViaAddress){

        }
        return true;
    }
    // </editor-fold>


    private boolean validateCoordinates(String lat, String lon){
        if(lat.isEmpty() || lon.isEmpty()){
            return false;
        }

        if(!lat.matches("[-0-9.]+") || !lon.matches("[-0-9.]+")){
            return false;
        }

        int latInt = Integer.parseInt(lat);
        int lonInt = Integer.parseInt(lon);

        if(latInt >= -90 && latInt <= 90 && lonInt >= -180 && lonInt <= 180){
            return true;
        }
        return false;
    }

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


    // <editor-fold desc="user tracking code">
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
    // </editor-fold>




    // <editor-fold desc="saving locations in map code">
    public void saveLocation(SavedLocation location, GeoPoint p){
        String locationId = location.getId();

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
    // </editor-fold>





    // <editor-fold desc="code for showing all users saved locations">
    private void showAllSavedLocations(){
        sharedPref_manager manager = new sharedPref_manager(mapAndLogic.this, "LoginUpdate");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(manager.getUsername()).child("SavedLocations");
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot locationSnapshot : task.getResult().getChildren()) {
                    String locationId = locationSnapshot.getKey();
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
    // </editor-fold>

    private void makeMarker(GeoPoint p, String address){
        Marker marker = new Marker(mapView);
        marker.setPosition(p);
        marker.setTitle(address);
        mapView.getOverlays().add(marker);
        mapView.invalidate();
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
        mapView.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback); // Pause map view
    }
}


