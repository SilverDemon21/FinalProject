package com.example.finalproject.adminStaff;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject.R;
import com.example.finalproject.ShowAllUsers.UserAdapter;
import com.example.finalproject.ShowAllUsers.UsersActivity;
import com.example.finalproject.mainAplication.GroupOfUsers;
import com.example.finalproject.mainAplication.SavedLocation;
import com.example.finalproject.sharedPref_manager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PendingGroups extends AppCompatActivity {

    private ListView listViewPendingGroups;
    private EditText etSearchPendingGroup;
    private PendingGroupsAdapter groupAdapter;
    private sharedPref_manager manager;
    List<GroupOfUsers> originalPendingGroups = new ArrayList<>();
    List<GroupOfUsers> pendingGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_groups);

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
    }

    private void fetchAllPendingGroups(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                originalPendingGroups = null;
                pendingGroups = null;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    GroupOfUsers checkGroup = dataSnapshot.getValue(GroupOfUsers.class);

                    if (checkGroup.getGroupState().equals("Pending")){
                        originalPendingGroups.add(checkGroup);
                        pendingGroups.add(checkGroup);
                    }
                }

                groupAdapter = new PendingGroupsAdapter(PendingGroups.this, pendingGroups);
                listViewPendingGroups.setAdapter(groupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void filterPendingGroups(String query) {
        List<GroupOfUsers> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(originalPendingGroups);
        } else {
            for (GroupOfUsers group : originalPendingGroups) {
                if (group.getGroupName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(group);
                }
            }
        }
        pendingGroups.clear();
        pendingGroups.addAll(filteredList);

        groupAdapter.notifyDataSetChanged();
    }
}