package com.example.finalproject.ShowAllUsers;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.RegestrationXLogin.signUpActivity;
import com.example.finalproject.User;
import com.example.finalproject.User_Profile;
import com.example.finalproject.sharedPref_manager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class UsersActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private ListView listViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private List<User> OriginalUserList = new ArrayList<>();
    private String currentUsername;
    private sharedPref_manager manager;
    private EditText etSearch;
    private Button btn_nameSort, btn_usernameSort;




    private static final String ADMIN_USERNAME = "admin";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_useres);

        etSearch = findViewById(R.id.etSearch);

        btn_nameSort = findViewById(R.id.btn_nameSort);
        btn_usernameSort = findViewById(R.id.btn_usernameSort);


        manager = new sharedPref_manager(UsersActivity.this,"LoginUpdate");
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        if(!manager.getUsername().equals("admin")){
            btn_nameSort.setVisibility(View.GONE);
            btn_usernameSort.setVisibility(View.GONE);
        }

        listViewUsers = findViewById(R.id.listViewUsers);
        currentUsername = manager.getUsername();

        btn_nameSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortUserListByName();
            }
        });

        btn_usernameSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortUserListByUsername();
            }
        });


        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterUserList(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        fetchUserData();

    }

    // Send the correct list of users to the adapter
    private void fetchUserData() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OriginalUserList.clear();
                userList.clear();

                boolean isAdmin = currentUsername.equals(ADMIN_USERNAME);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    if (isAdmin || snapshot.getKey().equals(currentUsername)) {
                        userList.add(user);
                        OriginalUserList.add(user);
                    }
                }


                userAdapter = new UserAdapter(UsersActivity.this, userList);
                listViewUsers.setAdapter(userAdapter);


                // Long click on the object will send to update the profile
                listViewUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        Object clickedItem = parent.getItemAtPosition(position);
                        User clickedUser = (User) clickedItem;
                        String ClickedUsername = clickedUser.getUsername();

                        if(currentUsername.equals(ClickedUsername)){
                            Intent intent = new Intent(UsersActivity.this, signUpActivity.class);
                            intent.putExtra("activity","update");
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(UsersActivity.this, "You are not this user pls click your stats", Toast.LENGTH_LONG).show();

                        }
                        return true;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Filter the users by their username if needed
    private void filterUserList(String query) {
        List<User> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(OriginalUserList);
        } else {
            for (User user : OriginalUserList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }
        userList.clear();
        userList.addAll(filteredList);

        userAdapter.notifyDataSetChanged();
    }


    private void sortUserListByName() {
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getName().compareToIgnoreCase(user2.getName());
            }
        });
        userAdapter.notifyDataSetChanged();
    }

    private void sortUserListByUsername() {
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getUsername().compareToIgnoreCase(user2.getUsername());
            }
        });
        userAdapter.notifyDataSetChanged();
    }







}