package com.example.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.mainAplication.ListUserGroups;
import com.example.finalproject.mainAplication.ServiceUserLocation;
import com.example.finalproject.mainAplication.mapAndLogic;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class User_Profile extends AppCompatActivity {
    private Button  deleteProfile;
    private TextView showEmail;
    sharedPref_manager manager;
    FirebaseDatabase database;
    DatabaseReference reference;

    int usersAmountOfSavedLocations = 0;
    int counterSavedLocations = 0;

    int usersAmountOfGroups = 0;
    int counterGroups = 0;

    int globalCheck = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        deleteProfile = findViewById(R.id.deleteProfile);
        manager = new sharedPref_manager(User_Profile.this, "LoginUpdate");

        if (!manager.getIsLoggedIn()){
            deleteProfile.setVisibility(View.GONE);
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBarProfile);

        bottomNavigationView.setSelectedItemId(R.id.menu_profile);

        bottomNavigationView.setOnItemSelectedListener(item ->{
            if(item.getItemId() == R.id.menu_home){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if(item.getItemId() == R.id.menu_map){
                startActivity(new Intent(getApplicationContext(), mapAndLogic.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if(item.getItemId() == R.id.menu_profile){
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

        // button to delete users profile
        deleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDelete();
            }
        });

    }


    public void createAlertDelete(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deleting the current profile");
        builder.setMessage("Are you sure?");
        builder.setCancelable(false);
        builder.setPositiveButton("Agree", new DeleteProfile());
        builder.setNegativeButton("Disagree", new DeleteProfile());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
    }


    private class DeleteProfile implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if(i == -1){
                sharedPref_manager manager = new sharedPref_manager(User_Profile.this,"LoginUpdate");
                DatabaseReference savedLocationsRef = FirebaseDatabase.getInstance().getReference().child("SavedLocations");
                DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");


                List<String> usersSavedLocations = new ArrayList<>();
                List<String> usersGroups = new ArrayList<>();


                usersRef.child(manager.getUsername()).child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            usersGroups.add(dataSnapshot.getKey());
                        }
                        usersAmountOfGroups = usersGroups.size();

                        if(usersGroups.isEmpty()){
                            checkIfCompleted();
                        }
                        for(String groupId: usersGroups){
                            groupsRef.child(groupId).child("groupUsers").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String status = snapshot.child(manager.getUsername()).getValue(String.class);
                                    if(status.equals("Manager")){
                                        groupsRef.child(groupId).removeValue();
                                    }
                                    else{
                                        groupsRef.child(groupId).child("groupUsers").child(manager.getUsername()).removeValue();
                                    }
                                    counterGroups++;

                                    if(counterGroups == usersAmountOfGroups){
                                        checkIfCompleted();
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

                usersRef.child(manager.getUsername()).child("SavedLocations").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            usersSavedLocations.add(dataSnapshot.getKey());
                        }
                        usersAmountOfSavedLocations = usersSavedLocations.size();

                        if(usersSavedLocations.isEmpty()){
                            checkIfCompleted();
                        }

                        for(String locationId: usersSavedLocations){
                            savedLocationsRef.child(locationId).removeValue();
                            counterSavedLocations++;

                            if(counterSavedLocations == usersAmountOfSavedLocations){
                                checkIfCompleted();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });



            }
        }
        private void checkIfCompleted(){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            if(globalCheck == 1){
                databaseReference.get().addOnCompleteListener(task -> {
                    String username = manager.getUsername();
                    String email = manager.getEmail();
                    String phone = manager.getPhoneNum();

                    databaseReference.child("users").child(username).removeValue();
                    databaseReference.child("emails").child(email).removeValue();
                    databaseReference.child("phoneNumbers").child(phone).removeValue();
                    manager.convertToLoggedOut();

                    StorageReference storageRef = FirebaseStorage.getInstance("gs://final-project-be550.firebasestorage.app").getReference();
                    StorageReference imageRef = storageRef.child(username);

                    imageRef.delete();

                    Intent serviceIntent = new Intent(User_Profile.this, ServiceUserLocation.class);
                    stopService(serviceIntent);

                    Intent intent = new Intent(User_Profile.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
            else{
                globalCheck++;
            }
        }
    }
}