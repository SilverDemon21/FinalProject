package com.example.finalproject.mainAplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.AllAdapters.AdapterSavedLocations;
import com.example.finalproject.AllObjects.Object_SavedLocation;
import com.example.finalproject.R;
import com.example.finalproject.sharedPref_manager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListOfSavedLocations extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private sharedPref_manager manager;
    private AdapterSavedLocations adapter;
    private ListView listViewSavedLocations;
    private EditText etSearchSavedLocation;
    List<Object_SavedLocation> originalLocations = new ArrayList<>();
    List<Object_SavedLocation> locations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_user_saved_locations);


        manager = new sharedPref_manager(ListOfSavedLocations.this,"LoginUpdate");

        listViewSavedLocations = findViewById(R.id.listViewSavedLocations);
        etSearchSavedLocation = findViewById(R.id.etSearchSavedLocation);


        etSearchSavedLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterSavedLocationList(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        listViewSavedLocations.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                Object_SavedLocation clickedLocation = (Object_SavedLocation) parent.getItemAtPosition(position);

                new AlertDialog.Builder(ListOfSavedLocations.this)
                        .setTitle("Delete Location")
                        .setMessage("Are you sure you want to delete this location: " +
                                clickedLocation.getAddress())
                        .setPositiveButton("Delete", ((dialog, which) -> {
                            deleteLocation(clickedLocation);
                        }))
                        .setNegativeButton("Cancel",null)
                        .show();

                return true;
            }
        });

        listViewSavedLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object_SavedLocation clickedLocation = locations.get(i);
                showNavigationChoiceDialog(ListOfSavedLocations.this, clickedLocation);
            }
        });


        fetchAllSavedLocations();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_button_go_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.menu_go_map){
            Intent intent = new Intent(ListOfSavedLocations.this, mapAndLogic.class);
            startActivity(intent);
            finish();
        }
        return true;
    }


    private void showNavigationChoiceDialog(Context context, Object_SavedLocation location){
        String[] options = {"Google Maps", "Waze"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

    private void fetchAllSavedLocations(){
        databaseReference.child("users").child(manager.getUsername()).child("SavedLocations")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> locationIds = new ArrayList<>();
                        for(DataSnapshot idSnapshot : snapshot.getChildren()){
                            locationIds.add(idSnapshot.getKey());
                        }

                        fetchLocationsDetails(locationIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void fetchLocationsDetails(List<String> locationIds){
        for(String locationId : locationIds) {
            databaseReference.child("SavedLocations").child(locationId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Object_SavedLocation location = snapshot.getValue(Object_SavedLocation.class);
                            if(location != null){
                                locations.add(location);
                                originalLocations.add(location);
                            }

                            if(locations.size() == locationIds.size()){
                                adapter = new AdapterSavedLocations(ListOfSavedLocations.this, locations);
                                listViewSavedLocations.setAdapter(adapter);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void filterSavedLocationList(String query) {
        List<Object_SavedLocation> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(originalLocations);
        } else {
            for (Object_SavedLocation location : originalLocations) {
                if (location.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(location);
                }
            }
        }
        locations.clear();
        locations.addAll(filteredList);

        adapter.notifyDataSetChanged();
    }


    private void deleteLocation(Object_SavedLocation location){
        String locationId = location.getId();
        String username = manager.getUsername();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();;
        databaseReference.child("SavedLocations").child(locationId).removeValue()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        databaseReference.child("users").child(username).child("SavedLocations").child(locationId)
                                .removeValue()
                                .addOnCompleteListener(userTask -> {
                                    if(userTask.isSuccessful()){
                                        locations.remove(location);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }


}