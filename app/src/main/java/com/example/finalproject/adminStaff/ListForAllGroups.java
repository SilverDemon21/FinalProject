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
import com.example.finalproject.mainAplication.Object_GroupOfUsers;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListForAllGroups extends AppCompatActivity {

    private ListView listViewAllGroupsOfUsers;
    private EditText etSearchGroupInAllGroups;
    List<Object_GroupOfUsers> originalAllGroups = new ArrayList<>();
    List<Object_GroupOfUsers> allGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
}