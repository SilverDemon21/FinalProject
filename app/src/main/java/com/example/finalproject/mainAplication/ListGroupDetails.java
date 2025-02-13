package com.example.finalproject.mainAplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.RegestrationXLogin.loginActivity;
import com.example.finalproject.ShowAllUsers.Object_User;
import com.example.finalproject.sharedPref_manager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListGroupDetails extends AppCompatActivity {
    private AdapterGroupDetails adapter;
    private ListView listViewDetailsOfUsersGroup;
    private EditText etSearchUserInGroup;
    List<Object_User> originalMembersInGroup = new ArrayList<>();
    List<Object_User> membersInGroup = new ArrayList<>();
    private Button btnAddPerson;

    private int counter;
    private int totalMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_group_details);

        listViewDetailsOfUsersGroup = findViewById(R.id.listViewDetailsOfUsersGroup);
        etSearchUserInGroup = findViewById(R.id.etSearchUserInGroup);
        btnAddPerson = findViewById(R.id.btnAddPerson);


        etSearchUserInGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterUsersInGroup(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnAddPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                LayoutInflater inflater = LayoutInflater.from(ListGroupDetails.this);
                View dialogView = inflater.inflate(R.layout.dialog_add_new_member_group, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(ListGroupDetails.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                EditText etMemberUsername = dialogView.findViewById(R.id.etMemberUsername);
                Button btnAddNewMemberByUsername = dialogView.findViewById(R.id.btnAddNewMemberByUsername);

                btnAddNewMemberByUsername.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(etMemberUsername.getText().toString().isEmpty()){
                            Toast.makeText(ListGroupDetails.this, "Pls enter a valid username", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            reference.child("users").child(etMemberUsername.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.exists()){
                                        Toast.makeText(ListGroupDetails.this, "The Username does not exist in the database", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Object_User user = snapshot.getValue(Object_User.class);
                                        String userEmail = user.getEmail();
                                        String groupId = getIntent().getStringExtra("GroupId");

                                        sendEmailInvitation(userEmail, user.getUsername(), groupId);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        fetchMembersInGroup();
    }

    private void fetchMembersInGroup(){
        String groupId = getIntent().getStringExtra("GroupId");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Groups").child(groupId).child("groupUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> groupIds = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    groupIds.add(dataSnapshot.getKey());
                }
                totalMembers = groupIds.size();
                List<String> membersStatus = new ArrayList<>();

                for(DataSnapshot memberSnapshot : snapshot.getChildren()){
                    String username = memberSnapshot.getValue().toString();
                    membersStatus.add(memberSnapshot.getKey());

                    databaseReference.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Object_User user = snapshot.getValue(Object_User.class);
                            membersInGroup.add(user);
                            originalMembersInGroup.add(user);
                            counter++;
                            if (counter == totalMembers){
                                adapter = new AdapterGroupDetails(ListGroupDetails.this, membersInGroup, membersStatus);
                                listViewDetailsOfUsersGroup.setAdapter(adapter);
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

    private void filterUsersInGroup(String query) {
        List<Object_User> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(originalMembersInGroup);
        } else {
            for (Object_User user : originalMembersInGroup) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }
        membersInGroup.clear();
        membersInGroup.addAll(filteredList);

        adapter.notifyDataSetChanged();
    }

    private void sendEmailInvitation(String userEmail, String username, String groupId) {
        // Create deep links using the custom scheme
        sharedPref_manager manager = new sharedPref_manager(ListGroupDetails.this, "LoginUpdate");
        String senderUsername = manager.getUsername();
        String acceptUrl = "myapp://accept?username=" + username + "&groupId=" + groupId;
        String declineUrl = "myapp://decline?username=" + username + "&groupId=" + groupId;

        String subject = "Group Invitation";
        String message = "You have been invited by" + senderUsername +"to join a group!\n\n" +
                "Click below to respond:\n\n" +
                "✅ Accept Invitation: " + acceptUrl + "\n" +
                "❌ Decline Invitation: " + declineUrl + "\n\n" +
                "Thank you!";

        // Send email intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e("Email", "No email clients installed.", ex);
        }
    }
}