package com.example.finalproject.mainAplication;

import org.osmdroid.config.Configuration;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.finalproject.MainActivity;
import com.example.finalproject.Permission;
import com.example.finalproject.R;
import com.example.finalproject.User_Profile;
import com.example.finalproject.sharedPref_manager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class mapAndLogic extends AppCompatActivity {

    private MapView mapView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker userMarker;
    private DatabaseReference databaseReference;
    private LocationCallback locationCallback;
    private Button btnShowSavedLocationsList;

    private Handler handler = new Handler();
    private Set<String> visibleUsers = new HashSet<>();
    private String currentUser;

    double latitude;
    double longitude;

    private Boolean settingConMapUser = true;
    private Boolean settingShowToastMapChange = true;

    private List<Marker> markerList = new ArrayList<>();


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
        currentUser = manager.getUsername();

        // <editor-fold desc="Bottom navigation bar setup">
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBarMap);

        bottomNavigationView.setSelectedItemId(R.id.menu_map);

        bottomNavigationView.setOnItemSelectedListener(item ->{
            if(item.getItemId() == R.id.menu_home){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            }
            else if(item.getItemId() == R.id.menu_map){
                return true;
            }
            else if(item.getItemId() == R.id.menu_profile){
                startActivity(new Intent(getApplicationContext(), User_Profile.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if(item.getItemId() == R.id.menu_groups){
                startActivity(new Intent(getApplicationContext(), ListUserGroups.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return true;
        });
        // </editor-fold>

        btnShowSavedLocationsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mapAndLogic.this, ListOfSavedLocations.class);
                finish();
                startActivity(intent);
            }
        });


        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        userRef.child(manager.getUsername()).child("profileSettings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                settingConMapUser = snapshot.child("concentrateOnUserMap").getValue(Boolean.class);
                settingShowToastMapChange = snapshot.child("showToastHelperMap").getValue(Boolean.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                for (Overlay overlay : mapView.getOverlays()) {
                    if (overlay instanceof Marker) {
                        Marker marker = (Marker) overlay;
                        // Check if this is a "custom" marker and if it's close to the tap location
                        if ("member".equals(marker.getRelatedObject()) && isMarkerTapped(marker, p)) {
                            Toast.makeText(mapView.getContext(), "Custom marker clicked: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                            showLocationOfMember(marker.getTitle());
                            return true; // Stop further event processing
                        }
                    }
                }
                return false; // Allow other overlays to handle the event
            }


            @Override
            public boolean longPressHelper(GeoPoint p) {
                // Handle long press for saving a location
                double latitude = p.getLatitude();
                double longitude = p.getLongitude();
                String address = getAddressFromCoordinates(latitude, longitude);

                sharedPref_manager manager = new sharedPref_manager(mapAndLogic.this, "LoginUpdate");
                Object_SavedLocation savedLocation = new Object_SavedLocation();
                savedLocation.setId(databaseReference.child("SavedLocations").push().getKey());
                savedLocation.setAddress(address);
                savedLocation.setLatitude(latitude);
                savedLocation.setLongitude(longitude);
                savedLocation.setUsername(manager.getUsername());

                showConfirmationSavingLocation(mapAndLogic.this, savedLocation, p);
                return true; // Return true to indicate that the event is handled
            }
        };



        MapEventsOverlay eventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapView.getOverlays().add(eventsOverlay);


        if (Permission.DoesUserHasAllOfThePermissions(mapAndLogic.this)) {
            startLocationUpdates();
            createNotificationChannel();
            startLocationService();
            showAllSavedLocations();
            startUpdatingMembersLocations();

        } else {
            Permission.GrantAllPermissions(mapAndLogic.this);
        }

    }

    // <editor-fold desc="Setting availability to track all the members">
    private boolean isMarkerTapped(Marker marker, GeoPoint tappedPoint) {
        double threshold = 0.0005; // ~50 meters
        return Math.abs(marker.getPosition().getLatitude() - tappedPoint.getLatitude()) < threshold &&
                Math.abs(marker.getPosition().getLongitude() - tappedPoint.getLongitude()) < threshold;
    }

    private void showLocationOfMember(String username){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");


        userRef.child(username).child("UserLocation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                latitude = Double.valueOf(snapshot.child("latitude").getValue(String.class));
                longitude = Double.valueOf(snapshot.child("longitude").getValue(String.class));
                String address = snapshot.child("address").getValue(String.class);

                Object_SavedLocation userLocation = new Object_SavedLocation();
                userLocation.setLatitude(latitude);
                userLocation.setLongitude(longitude);
                userLocation.setAddress(address);

                showNavigationChoiceDialog(mapAndLogic.this, userLocation);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showNavigationChoiceDialog(Context context, Object_SavedLocation location){
        String[] options = {"Google Maps", "Waze"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Choose Navigation App");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean useGoogleMaps = (i == 0);
                NavigationHelper.navigateToLocation(context, location, useGoogleMaps);
            }
        });

        builder.show();
    }
    // </editor-fold>


    // <editor-fold desc="Fetching all members locations">
    public void startUpdatingMembersLocations() {
        fetchVisibleUsers();
        fetchUserLocations(visibleUsers);
        handler.postDelayed(updateUserListRunnable, 11000);
        handler.postDelayed(updateUserLocationsRunnable, 7000);
    }

    private Runnable updateUserListRunnable = new Runnable() {
        @Override
        public void run() {
            fetchVisibleUsers();
            handler.postDelayed(this, 11000);
        }
    };

    private Runnable updateUserLocationsRunnable = new Runnable() {
        @Override
        public void run() {
            fetchUserLocations(visibleUsers);
            handler.postDelayed(this, 7000);
        }
    };

    public void stopUpdatingUserLocations() {
        handler.removeCallbacks(updateUserListRunnable);
        handler.removeCallbacks(updateUserLocationsRunnable);
    }

    private void fetchVisibleUsers(){
        DatabaseReference userGroupRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser).child("Groups");
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        if(settingShowToastMapChange){
            Toast.makeText(mapAndLogic.this, "fetching members list", Toast.LENGTH_SHORT).show();
        }
        userGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                visibleUsers.clear();

                for(DataSnapshot groupSnapshot : snapshot.getChildren()){
                    String groupId = groupSnapshot.getKey();

                    groupRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()) return;

                            if(!snapshot.child("groupState").equals("Pending")){
                                String groupType = snapshot.child("groupType").getValue(String.class);
                                DataSnapshot memberSnapshot = snapshot.child("groupUsers");

                                boolean isManagerOrCoManager = false;

                                for(DataSnapshot member : memberSnapshot.getChildren()){
                                    String memberUsername = member.getKey();
                                    String role = member.getValue(String.class);

                                    if((memberUsername.equals(currentUser) && role.equals("Manager")) || (memberUsername.equals(currentUser) && role.equals("CoManager"))){
                                        isManagerOrCoManager = true;
                                    }
                                }

                                for (DataSnapshot member : memberSnapshot.getChildren()){
                                    String memberUsername = member.getKey();
                                    String memberRole = member.getValue(String.class);
                                    if (!memberUsername.equals(currentUser)) {
                                        if (groupType.equals("Friends Mode") && !visibleUsers.contains(memberUsername)){
                                            visibleUsers.add(memberUsername);
                                        }
                                        else if (groupType.equals("Parents Mode") && isManagerOrCoManager && memberRole.equals("Member") && !visibleUsers.contains(memberUsername)) {
                                            visibleUsers.add(memberUsername);
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchUserLocations(Set<String> userNames) {
        removePrevMembersMarkers();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        if(settingShowToastMapChange){
            Toast.makeText(mapAndLogic.this, "showing members on map", Toast.LENGTH_SHORT).show();
        }
        for (String username : userNames) {
            usersRef.child(username).child("UserLocation").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String latitude = snapshot.child("latitude").getValue(String.class);
                        String longitude = snapshot.child("longitude").getValue(String.class);
                        usersRef.child(username).child("photoUrl").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                String photoUrl = dataSnapshot.getValue(String.class);
                                showUserOnMap(username, Double.parseDouble(latitude), Double.parseDouble(longitude), photoUrl);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Error fetching user location", error.toException());
                }
            });
        }
    }

    private void showUserOnMap(String username, Double latitude, Double longitude, String photoUrl){
        Marker memberMarker = new Marker(mapView);
        GeoPoint point = new GeoPoint(latitude, longitude);
        memberMarker.setPosition(point);
        memberMarker.setTitle(username);
        Glide.with(this)
                .asBitmap()
                .load(photoUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Resize the image
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(resource, 90, 90, false);
                        Bitmap circularBitmap = getCircularBitmap(resizedBitmap);

                        // Set the resized image as the marker icon
                        memberMarker.setIcon(new BitmapDrawable(getResources(), circularBitmap));
                        mapView.getOverlays().add(memberMarker);
                        mapView.invalidate();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle placeholder if needed
                    }
                });

        memberMarker.setRelatedObject("member");
        markerList.add(memberMarker);
    }

    private void removePrevMembersMarkers(){
        for(Marker marker : markerList){
            if(marker.getRelatedObject().equals("member")){
                marker.remove(mapView);
            }
        }
        markerList = new ArrayList<>();
    }
    // </editor-fold>


    // <editor-fold desc="Circular image setup for user image on the map">
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
                        Object_SavedLocation location = new Object_SavedLocation();
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
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.dialog_make_saved_location_via_address, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            EditText etAddressForSaving = dialogView.findViewById(R.id.etAddressForSaving);
            EditText etTitleForSavingAddress = dialogView.findViewById(R.id.etTitleForSavingAddress);
            Button btnCreateSavedLocationViaAddress = dialogView.findViewById(R.id.btnCreateSavedLocationViaAddress);

            btnCreateSavedLocationViaAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String address = etAddressForSaving.getText().toString().trim();
                    String Title = etTitleForSavingAddress.getText().toString().trim();
                    double[] coordinates = getCoordinatesFromAddress(address);
                    sharedPref_manager manager = new sharedPref_manager(mapAndLogic.this, "LoginUpdate");

                    if(coordinates != null){
                        Double latitude = coordinates[0];
                        Double longitude = coordinates[1];

                        Object_SavedLocation location = new Object_SavedLocation();
                        location.setTitle(Title);
                        location.setLatitude(latitude);
                        location.setLongitude(longitude);
                        location.setUsername(manager.getUsername());
                        location.setId(databaseReference.child("SavedLocations").push().getKey());
                        location.setAddress(address);

                        GeoPoint p = new GeoPoint(latitude, longitude);

                        saveLocation(location, p);

                        dialog.dismiss();
                    }
                }
            });
            dialog.show();
        }


        return true;
    }
    // </editor-fold>


    // <editor-fold desc="Coordinates validation">
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
    // </editor-fold>


    // <editor-fold desc="User tracking code">
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
            if(settingConMapUser){
                mapView.getController().animateTo(userLocation);
            }
            mapView.invalidate();

            if(settingShowToastMapChange){
                Toast.makeText(this, "new location", Toast.LENGTH_SHORT).show();
            }

        }
    }
    // </editor-fold>


    // <editor-fold desc="Saving locations in map code">
    public void saveLocation(Object_SavedLocation location, GeoPoint p){
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

        makeMarker(p, location.getTitle());
    }

    public void showConfirmationSavingLocation(Context context, Object_SavedLocation location, GeoPoint p){
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


    // <editor-fold desc="Showing all users saved locations on map">
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
                            String title = locationTask.getResult().child("title").getValue(String.class);

                            // Add marker to the map
                            GeoPoint point = new GeoPoint(latitude, longitude);
                            makeMarker(point, title);
                        }
                    });
                }
            } else {
                Toast.makeText(this, "No saved locations found", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // </editor-fold>


    // <editor-fold desc="Adds a marker on the map">
    private void makeMarker(GeoPoint p, String address){
        Marker marker = new Marker(mapView);
        marker.setPosition(p);
        marker.setTitle(address);
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }
    // </editor-fold>


    // <editor-fold desc="Conversion between address and coordinates">
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

    private double[] getCoordinatesFromAddress(String address){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        double[] coordinates = new double[2];

        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if(addressList != null && !addressList.isEmpty()){
                Address location = addressList.get(0);
                coordinates[0] = location.getLatitude();
                coordinates[1] = location.getLongitude();
            }
            else{
                coordinates = null;
            }
        }
        catch (IOException e){
            e.printStackTrace();
            coordinates = null;
        }
        return coordinates;
    }
    // </editor-fold>


    // <editor-fold desc="Creates notification channel for the service">
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "location_channel";
            String channelName = "Location Tracking";
            String channelDescription = "Notifications for location tracking in the background";
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    // </editor-fold>


    // <editor-fold desc="Starting and stopping location service">
    private void startLocationService(){
        Intent serviceIntent = new Intent(this, ServiceUserLocation.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopLocationService(){
        Intent serviceIntent = new Intent(this, ServiceUserLocation.class);
        stopService(serviceIntent);
    }
    // </editor-fold>


    // <editor-fold desc="Configuration of all app states">
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
                createNotificationChannel();
                startLocationService();
                showAllSavedLocations();
                startUpdatingMembersLocations();
            } else {
                Toast.makeText(this, "Permission denied! Cannot access location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); // Resume map view

        if (Permission.DoesUserHasAllOfThePermissions(mapAndLogic.this)) { // Check if permissions are granted
            startLocationUpdates(); // Restart location tracking
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        stopUpdatingUserLocations();
        // Pause map view
    }

    // </editor-fold>
}


