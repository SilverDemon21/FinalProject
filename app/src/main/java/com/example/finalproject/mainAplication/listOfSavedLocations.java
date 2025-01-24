package com.example.finalproject.mainAplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
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

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.sharedPref_manager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class listOfSavedLocations extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private sharedPref_manager manager;
    private LocationAdapter adapter;
    private ListView listViewSavedLocations;
    private EditText etSearchSavedLocation;
    List<SavedLocation> originalLocations = new ArrayList<>();
    List<SavedLocation> locations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_saved_locations);


        manager = new sharedPref_manager(listOfSavedLocations.this,"LoginUpdate");

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
                SavedLocation clickedLocation = (SavedLocation) parent.getItemAtPosition(position);

                new AlertDialog.Builder(listOfSavedLocations.this)
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
                SavedLocation clickedLocation = locations.get(i);
                showNavigationChoiceDialog(listOfSavedLocations.this, clickedLocation);
            }
        });


        fetchAllSavedLocations();
    }

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
            Intent intent = new Intent(listOfSavedLocations.this, MainActivity.class);
            startActivity(intent);
        }
        return true;
    }


    private void showNavigationChoiceDialog(Context context, SavedLocation location){
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
                            SavedLocation location = snapshot.getValue(SavedLocation.class);
                            if(location != null){
                                locations.add(location);
                                originalLocations.add(location);
                            }

                            if(locations.size() == locationIds.size()){
                                adapter = new LocationAdapter(listOfSavedLocations.this, locations);
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
        List<SavedLocation> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(originalLocations);
        } else {
            for (SavedLocation location : originalLocations) {
                if (location.getAddress().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(location);
                }
            }
        }
        locations.clear();
        locations.addAll(filteredList);

        adapter.notifyDataSetChanged();
    }


    private void deleteLocation(SavedLocation location){
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