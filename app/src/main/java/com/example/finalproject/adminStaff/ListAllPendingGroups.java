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

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.mainAplication.ListUserGroups;
import com.example.finalproject.mainAplication.Object_GroupOfUsers;
import com.example.finalproject.mainAplication.mapAndLogic;
import com.example.finalproject.sharedPref_manager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListAllPendingGroups extends AppCompatActivity {

    private ListView listViewPendingGroups;
    private EditText etSearchPendingGroup;
    private AdapterAllPendingGroups groupAdapter;
    private sharedPref_manager manager;
    List<Object_GroupOfUsers> originalPendingGroups = new ArrayList<>();
    List<Object_GroupOfUsers> pendingGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_all_pending_groups);

        listViewPendingGroups = findViewById(R.id.listViewPendingGroups);
        etSearchPendingGroup = findViewById(R.id.etSearchPendingGroup);


        etSearchPendingGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterPendingGroups(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        fetchAllPendingGroups();


        listViewPendingGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object_GroupOfUsers clickedGroup = pendingGroups.get(i);

                new AlertDialog.Builder(ListAllPendingGroups.this)
                        .setTitle("Accept group")
                        .setMessage("Are you sure you want to accept this group?: " +
                                clickedGroup.getGroupName() + "created by" + clickedGroup.getGroupUsers().keySet().iterator().next())
                        .setPositiveButton("accept", ((dialog, which) -> {
                            acceptGroup(clickedGroup);
                        }))
                        .setNegativeButton("decline",(dialog, which) -> {
                           removeGroup(clickedGroup);
                        })
                        .show();
            }
        });

    }


    private void fetchAllPendingGroups(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Object_GroupOfUsers checkGroup = dataSnapshot.getValue(Object_GroupOfUsers.class);

                    if (checkGroup.getGroupState().equals("Pending")){
                        originalPendingGroups.add(checkGroup);
                        pendingGroups.add(checkGroup);
                    }
                }

                groupAdapter = new AdapterAllPendingGroups(ListAllPendingGroups.this, pendingGroups);
                listViewPendingGroups.setAdapter(groupAdapter);
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
            Intent intent = new Intent(ListAllPendingGroups.this, ListUserGroups.class);
            startActivity(intent);
        }
        return true;
    }

    private void filterPendingGroups(String query) {
        List<Object_GroupOfUsers> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(originalPendingGroups);
        } else {
            for (Object_GroupOfUsers group : originalPendingGroups) {
                if (group.getGroupName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(group);
                }
            }
        }
        pendingGroups.clear();
        pendingGroups.addAll(filteredList);

        groupAdapter.notifyDataSetChanged();
    }


    private void acceptGroup(Object_GroupOfUsers group){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Groups").child(group.getGroupId()).child("groupState").setValue("Active");
        originalPendingGroups.remove(group);
        pendingGroups.remove(group);
        groupAdapter.notifyDataSetChanged();

    }

    private void removeGroup(Object_GroupOfUsers group){
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        String creatorUsername = group.getGroupUsers().entrySet().iterator().next().getKey();
        userRef.child(creatorUsername).child("Groups").child(group.getGroupId()).removeValue();
        groupRef.child(group.getGroupId()).removeValue();

        originalPendingGroups.remove(group);
        pendingGroups.remove(group);
        groupAdapter.notifyDataSetChanged();

    }
}