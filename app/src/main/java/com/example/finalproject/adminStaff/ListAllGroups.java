package com.example.finalproject.adminStaff;

import android.app.AlertDialog;
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

import com.example.finalproject.AllAdapters.AdapterForAllGroups;
import com.example.finalproject.R;
import com.example.finalproject.mainAplication.ListUserGroups;
import com.example.finalproject.AllObjects.Object_GroupOfUsers;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListAllGroups extends AppCompatActivity {

    private ListView listViewAllGroupsOfUsers;
    private EditText etSearchGroupInAllGroups;
    List<Object_GroupOfUsers> originalAllGroups = new ArrayList<>();
    List<Object_GroupOfUsers> allGroups = new ArrayList<>();
    private AdapterForAllGroups groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_for_all_groups);

        listViewAllGroupsOfUsers = findViewById(R.id.listViewAllGroupsOfUsers);
        etSearchGroupInAllGroups = findViewById(R.id.etSearchGroupInAllGroups);

        etSearchGroupInAllGroups.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterAllAppGroups(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        listViewAllGroupsOfUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object_GroupOfUsers clickedGroup = allGroups.get(i);

                new AlertDialog.Builder(ListAllGroups.this)
                        .setTitle("Delete Users group")
                        .setMessage("Are you sure you want to delete this group?")
                        .setPositiveButton("Delete", ((dialog, which) -> {
                            deleteGroup(clickedGroup);
                        }))
                        .setNegativeButton("decline",null)
                        .show();
                return false;
            }
        });

        fetchAllGroupsApp();
    }



    private void fetchAllGroupsApp(){
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Object_GroupOfUsers group = dataSnapshot.getValue(Object_GroupOfUsers.class);
                    allGroups.add(group);
                    originalAllGroups.add(group);
                }

                groupAdapter = new AdapterForAllGroups(ListAllGroups.this, allGroups);
                listViewAllGroupsOfUsers.setAdapter(groupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_button_go_groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.menu_go_groups){
            Intent intent = new Intent(ListAllGroups.this, ListUserGroups.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    private void filterAllAppGroups(String query) {
        List<Object_GroupOfUsers> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(originalAllGroups);
        } else {
            for (Object_GroupOfUsers group : originalAllGroups) {
                if (group.getGroupName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(group);
                }
            }
        }
        allGroups.clear();
        allGroups.addAll(filteredList);

        groupAdapter.notifyDataSetChanged();
    }

    private void deleteGroup(Object_GroupOfUsers group){
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        for(String username : group.getGroupUsers().keySet()){
            usersRef.child(username).child("Groups").child(group.getGroupId()).removeValue();
        }

        groupRef.child(group.getGroupId()).removeValue();


        originalAllGroups.remove(group);
        allGroups.remove(group);
        groupAdapter.notifyDataSetChanged();

    }
}