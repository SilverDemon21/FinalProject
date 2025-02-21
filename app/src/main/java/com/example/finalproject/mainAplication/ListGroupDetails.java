package com.example.finalproject.mainAplication;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.example.finalproject.adminStaff.ListAllPendingGroups;
import com.example.finalproject.sharedPref_manager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListGroupDetails extends AppCompatActivity {
    private AdapterGroupDetails adapter;
    private ListView listViewDetailsOfUsersGroup;
    private EditText etSearchUserInGroup;
    List<Object_User> originalMembersInGroup = new ArrayList<>();
    List<Object_User> membersInGroup = new ArrayList<>();
    private String managerUsername;
    private String groupId;
    private Button btnAddPerson, btnDeleteGroup;

    private int counter;
    private int totalMembers;

    int amountToDelete = 0;
    int counterToDelete = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_group_details);

        listViewDetailsOfUsersGroup = findViewById(R.id.listViewDetailsOfUsersGroup);
        etSearchUserInGroup = findViewById(R.id.etSearchUserInGroup);
        btnAddPerson = findViewById(R.id.btnAddPerson);
        btnDeleteGroup = findViewById(R.id.btnDeleteGroup);

        managerUsername = getIntent().getStringExtra("GroupManager");
        groupId = getIntent().getStringExtra("GroupId");


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

        btnDeleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPref_manager manager = new sharedPref_manager(ListGroupDetails.this, "LoginUpdate");
                if(manager.getUsername().equals(managerUsername)){
                    new android.app.AlertDialog.Builder(ListGroupDetails.this)
                            .setTitle("Delete group")
                            .setMessage("Are you sure you want to delete this group?")
                            .setPositiveButton("Delete", ((dialog, which) -> {
                                deleteGroup();
                            }))
                            .setNegativeButton("Cancel",null)
                            .show();
                }
            }
        });

        listViewDetailsOfUsersGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                sharedPref_manager manager = new sharedPref_manager(ListGroupDetails.this, "LoginUpdate");
                if(manager.getUsername().equals(managerUsername)){
                    Object_User user = (Object_User) parent.getItemAtPosition(position);

                    if(!user.getUsername().equals(managerUsername)){
                        new android.app.AlertDialog.Builder(ListGroupDetails.this)
                                .setTitle("Promote Member")
                                .setMessage("Are you sure you want to promote this member: " +
                                        user.getUsername())
                                .setPositiveButton("Promote", ((dialog, which) -> {
                                    promoteMember(user);
                                }))
                                .setNegativeButton("Cancel",null)
                                .show();
                    }
                    else{
                        Toast.makeText(ListGroupDetails.this, "You cant promote yourself", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(ListGroupDetails.this, "Only the manager of the group can promote other members", Toast.LENGTH_SHORT).show();
                }
            }
        });

        listViewDetailsOfUsersGroup.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                sharedPref_manager manager = new sharedPref_manager(ListGroupDetails.this, "LoginUpdate");
                if(manager.getUsername().equals(managerUsername)){
                    Object_User user = (Object_User) parent.getItemAtPosition(position);

                    if(!user.getUsername().equals(managerUsername)){
                        new android.app.AlertDialog.Builder(ListGroupDetails.this)
                                .setTitle("Delete Member")
                                .setMessage("Are you sure you want to delete this member: " +
                                        user.getUsername())
                                .setPositiveButton("Delete", ((dialog, which) -> {
                                    deleteMember(user);
                                }))
                                .setNegativeButton("Cancel",null)
                                .show();
                    }
                    else{
                        Toast.makeText(ListGroupDetails.this, "You cant remove yourself from the group", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(ListGroupDetails.this, "Only the manager of the group can remove other members", Toast.LENGTH_SHORT).show();
                }
                return true;
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
                    String username = memberSnapshot.getKey();
                    membersStatus.add(memberSnapshot.getValue().toString());

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
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
            Intent intent = new Intent(ListGroupDetails.this, ListUserGroups.class);
            startActivity(intent);
        }
        return true;
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

        // Format the message as HTML to allow clickable links
        String htmlMessage = "<html><body>" +
                "<p>You have been invited by <strong>" + senderUsername + "</strong> to join a group!</p>" +
                "<p>Click below to respond:</p>" +
                "<p><a href='" + acceptUrl + "'>✅ Accept Invitation</a></p>" +
                "<p><a href='" + declineUrl + "'>❌ Decline Invitation</a></p>" +
                "<p>Or reply to this email to accept manually.</p>" +
                "<p>Thank you!</p>" +
                "</body></html>";

        // Plain text fallback (for email clients that do not support HTML)
        String plainTextMessage = "You have been invited by " + senderUsername + " to join a group!\n\n"
                + "✅ Accept Invitation: " + acceptUrl + "\n"
                + "❌ Decline Invitation: " + declineUrl + "\n\n"
                + "Thank you!";

        // Create email intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");  // Ensures only email apps handle it
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(htmlMessage, Html.FROM_HTML_MODE_LEGACY)); // Ensure HTML formatting
        intent.putExtra(Intent.EXTRA_HTML_TEXT, htmlMessage);  // Use HTML text if supported
        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e("Email", "No email clients installed.", ex);
        }
    }


    private void deleteMember(Object_User clickedMember){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(clickedMember.getUsername()).child("Groups");
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);

        userRef.child(groupId).removeValue();
        groupRef.child("groupUsers").child(clickedMember.getUsername()).removeValue();
    }

    private void promoteMember(Object_User clickedMember){
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);

        groupRef.child("groupUsers").child(clickedMember.getUsername()).setValue("CoManager");
    }

    private void deleteGroup(){
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        groupRef.child("groupUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot countSnapshot : snapshot.getChildren()){
                    amountToDelete += 1;
                }

                for(DataSnapshot userSnapshot : snapshot.getChildren()){
                    userRef.child(userSnapshot.getKey()).child("Groups").child(groupId).removeValue();
                    counterToDelete++;

                    if(counterToDelete == amountToDelete){
                        groupRef.removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });



        Intent intent = new Intent(ListGroupDetails.this, ListUserGroups.class);
        startActivity(intent);
    }
}