package com.example.finalproject.mainAplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.finalproject.AllAdapters.AdapterUserGroups;
import com.example.finalproject.AllObjects.Object_GroupOfUsers;
import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.UserProfileActivity;
import com.example.finalproject.adminStaff.ListAllPendingGroups;
import com.example.finalproject.adminStaff.ListAllGroups;
import com.example.finalproject.sharedPref_manager;
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

    private Button btnCreateGroup, btnJoinGroupStart;
    private boolean itemIsSelected = false;
    private String selectedOption;
    private AdapterUserGroups groupAdapter;
    private ListView listViewUsersGroups;
    private EditText etSearchUserGroup;
    List<Object_GroupOfUsers> originalUserGroups = new ArrayList<>();
    List<Object_GroupOfUsers> userGroups = new ArrayList<>();
    private int counter, totalGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_user_groups);

        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        listViewUsersGroups = findViewById(R.id.listViewUsersGroups);
        etSearchUserGroup = findViewById(R.id.etSearchUserGroup);
        btnJoinGroupStart = findViewById(R.id.btnJoinGroupStart);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBarGroups);

        bottomNavigationView.setSelectedItemId(R.id.menu_groups);




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
                startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
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

                intent.putExtra("GroupName", clickedGroup.getGroupName());


                startActivity(intent);
                finish();
            }
        });

        btnJoinGroupStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPref_manager manager = new sharedPref_manager(ListUserGroups.this, "LoginUpdate");

                LayoutInflater inflater = LayoutInflater.from(ListUserGroups.this);
                View dialogView = inflater.inflate(R.layout.dialog_join_group, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(ListUserGroups.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                EditText etGroupIdForJoining = dialogView.findViewById(R.id.etGroupIdForJoining);
                Button btnJoinGroupStart = dialogView.findViewById(R.id.btnJoinGroup);

                btnJoinGroupStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(etGroupIdForJoining.getText().toString().isEmpty()){
                            Toast.makeText(ListUserGroups.this, "Pls enter a group id", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        else{
                            DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
                            groupRef.child(etGroupIdForJoining.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.exists()){
                                        Toast.makeText(ListUserGroups.this, "There is no such group with this id", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                    else{
                                        groupRef.child(etGroupIdForJoining.getText().toString()).child("pendingMembers").child(manager.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(!snapshot.exists()){
                                                    Toast.makeText(ListUserGroups.this,"You have not been invited to join this group",Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }
                                                else{
                                                    addUserToGroup(etGroupIdForJoining.getText().toString(), manager.getUsername());
                                                    dialog.dismiss();
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                    }
                });
                dialog.show();
            }
        });
    }

    private void addUserToGroup(String groupId, String username){
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        groupRef.child(groupId).child("pendingMembers").child(username).removeValue();

        HashMap<String, Object> updatesForGroup = new HashMap<>();
        updatesForGroup.put(username, "Member");
        groupRef.child(groupId).child("groupUsers").updateChildren(updatesForGroup);

        HashMap<String, Object> updateForUser = new HashMap<>();
        updateForUser.put(groupId, true);
        userRef.child(username).child("Groups").updateChildren(updateForUser);

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

        userGroups.add(newUsersGroup);
        originalUserGroups.add(newUsersGroup);

        databaseReference.child("users").child(senderUsername).updateChildren(updates);

        listViewUsersGroups.invalidateViews();
        listViewUsersGroups.refreshDrawableState();


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
            finish();
        }
        else if(item.getItemId() == R.id.menu_allGroupsForAdmin){
            Intent intent = new Intent(ListUserGroups.this, ListAllGroups.class);
            startActivity(intent);
            finish();
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