package com.example.finalproject.mainAplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.User_Profile;
import com.example.finalproject.adminStaff.ListAllPendingGroups;
import com.example.finalproject.adminStaff.ListForAllGroups;
import com.example.finalproject.sharedPref_manager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListUserGroups extends AppCompatActivity {

    private Button btnCreateGroup;
    private boolean itemIsSelected = false;
    private String selectedOption;
    private AdapterUserGroups groupAdapter;
    private ListView listViewUsersGroups;
    private EditText etSearchUserGroup;
    List<Object_GroupOfUsers> originalUserGroups = new ArrayList<>();
    List<Object_GroupOfUsers> userGroups = new ArrayList<>();
    private int counter;
    private int totalGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_user_groups);

        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        listViewUsersGroups = findViewById(R.id.listViewUsersGroups);
        etSearchUserGroup = findViewById(R.id.etSearchUserGroup);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBarGroups);

        bottomNavigationView.setSelectedItemId(R.id.menu_groups);




        bottomNavigationView.setOnItemSelectedListener(item ->{
            if(item.getItemId() == R.id.menu_home){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if(item.getItemId() == R.id.menu_map){
                startActivity(new Intent(getApplicationContext(), mapAndLogic.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if(item.getItemId() == R.id.menu_profile){
                startActivity(new Intent(getApplicationContext(), User_Profile.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if(item.getItemId() == R.id.menu_groups){
                return true;
            }
            return true;
        });


        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sharedPref_manager manager = new sharedPref_manager(ListUserGroups.this, "LoginUpdate");

                LayoutInflater inflater = LayoutInflater.from(ListUserGroups.this);
                View dialogView = inflater.inflate(R.layout.dialog_create_new_group, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(ListUserGroups.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                EditText etGroupName = dialogView.findViewById(R.id.etGroupName);
                Button btnCreateGroup = dialogView.findViewById(R.id.btnCreateGroup);
                Spinner spinner = dialogView.findViewById(R.id.spinnerGroupType);

                String[] options = {"Parents Mode", "Friends Mode"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ListUserGroups.this, android.R.layout.simple_spinner_dropdown_item, options);
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedOption = options[i];
                        itemIsSelected = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                btnCreateGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendRequestToAdmin(etGroupName.getText().toString().trim(), selectedOption, manager.getUsername());
                        Toast.makeText(ListUserGroups.this, "Your request to open new group was redirected to the admin", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
        etSearchUserGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterUserGroups(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        fetchUsersGroups();

        listViewUsersGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object_GroupOfUsers clickedGroup = userGroups.get(i);

                if(clickedGroup.getGroupState().equals("Pending")){
                    Toast.makeText(ListUserGroups.this, "The group is pending you cannot access it", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(ListUserGroups.this, ListGroupDetails.class);
                intent.putExtra("GroupId", clickedGroup.getGroupId());
                clickedGroup.getGroupUsers().entrySet().stream()
                                .filter(entry -> entry.getValue().equals("Manager"))
                                .findFirst()
                                .ifPresent(entry ->intent.putExtra("GroupManager", entry.getKey()));


                startActivity(intent);
            }
        });

        Uri data = getIntent().getData();
        if (data != null) {
            String path = data.getPath();  // e.g., /accept or /decline
            String username = data.getQueryParameter("username");
            String groupId = data.getQueryParameter("groupId");

            if (path != null && username != null && groupId != null) {
                if (path.equals("/accept")) {
                    Log.d("DeepLink", "User accepted: " + username);
                    handleGroupRequest(username, groupId, true);
                } else if (path.equals("/decline")) {
                    Log.d("DeepLink", "User declined: " + username);
                    handleGroupRequest(username, groupId, false);
                }
            }
        }

    }

    private void handleGroupRequest(String username, String groupId, boolean usersAnswer){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        if(usersAnswer){
            Map<String, Object> updateForUser = new HashMap<>();
            updateForUser.put(groupId, true);
            reference.child("users").child(username).child("Groups").updateChildren(updateForUser);

            Map<String, Object> updateForGroup = new HashMap<>();
            updateForGroup.put(username, "Member");
            reference.child("Groups").child(groupId).child("groupUsers").updateChildren(updateForGroup);
        }
    }

    private void fetchUsersGroups(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        sharedPref_manager manager = new sharedPref_manager(ListUserGroups.this, "LoginUpdate");

        database.child("users").child(manager.getUsername()).child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<String> groupIds = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        groupIds.add(dataSnapshot.getKey()); // Collect all group IDs
                    }

                    counter = 0;
                    totalGroups = groupIds.size();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String groupId = dataSnapshot.getKey();

                        database.child("Groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    Object_GroupOfUsers checkGroup = snapshot.getValue(Object_GroupOfUsers.class);
                                    userGroups.add(checkGroup);
                                    originalUserGroups.add(checkGroup);
                                    counter++;

                                    if(counter == totalGroups){
                                        groupAdapter = new AdapterUserGroups(ListUserGroups.this, userGroups);
                                        listViewUsersGroups.setAdapter(groupAdapter);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    public void sendRequestToAdmin(String groupName, String groupType, String senderUsername){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String groupId = databaseReference.child("UserGroups").push().getKey();
        HashMap<String, String> firstUserInGroupMap = new HashMap<>();
        firstUserInGroupMap.put(senderUsername, "Manager");

        Object_GroupOfUsers newUsersGroup = new Object_GroupOfUsers(groupId, groupType, groupName ,firstUserInGroupMap);
        databaseReference.child("Groups").child(groupId).setValue(newUsersGroup);

        Map<String,Object> updates = new HashMap<>();
        updates.put("Groups/" + groupId,true);
        databaseReference.child("users").child(senderUsername).updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listViewUsersGroups.invalidateViews();
                listViewUsersGroups.refreshDrawableState();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        sharedPref_manager manager = new sharedPref_manager(ListUserGroups.this, "LoginUpdate");
        getMenuInflater().inflate(R.menu.menu_groups_options, menu);
        MenuItem item1 = menu.findItem(R.id.menu_pendingGroupsAccepts);
        MenuItem item2 = menu.findItem(R.id.menu_allGroupsForAdmin);

        if(!manager.getUsername().equals("admin")){
            item2.setVisible(false);
            item1.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.menu_pendingGroupsAccepts){
            Intent intent = new Intent(ListUserGroups.this, ListAllPendingGroups.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.menu_allGroupsForAdmin){
            Intent intent = new Intent(ListUserGroups.this, ListForAllGroups.class);
            startActivity(intent);
        }
        return true;
    }

    private void filterUserGroups(String query) {
        List<Object_GroupOfUsers> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(originalUserGroups);
        } else {
            for (Object_GroupOfUsers group : originalUserGroups) {
                if (group.getGroupName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(group);
                }
            }
        }

        userGroups.clear();
        userGroups.addAll(filteredList);

        groupAdapter.notifyDataSetChanged();
    }
}