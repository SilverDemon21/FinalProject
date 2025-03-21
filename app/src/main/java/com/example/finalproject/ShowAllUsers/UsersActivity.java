package com.example.finalproject.ShowAllUsers;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.RegestrationXLogin.signUpActivity;
import com.example.finalproject.sharedPref_manager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class UsersActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private ListView listViewUsers;
    private UserAdapter userAdapter;
    private List<Object_User> objectUserList = new ArrayList<>();
    private List<Object_User> originalObjectUserList = new ArrayList<>();
    private String currentUsername;
    private sharedPref_manager manager;
    private EditText etSearch;
    private Button btn_ageSort, btn_usernameSort;




    private static final String ADMIN_USERNAME = "admin";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_all_users);

        etSearch = findViewById(R.id.etSearch);

        btn_ageSort = findViewById(R.id.btn_nameSort);
        btn_usernameSort = findViewById(R.id.btn_usernameSort);


        manager = new sharedPref_manager(UsersActivity.this,"LoginUpdate");
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        if(!manager.getUsername().equals("admin")){
            btn_ageSort.setVisibility(View.GONE);
            btn_usernameSort.setVisibility(View.GONE);
            etSearch.setVisibility(View.GONE);
        }

        listViewUsers = findViewById(R.id.listViewUsers);
        currentUsername = manager.getUsername();

        btn_ageSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortUserListByAge();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_button_go_back_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.menu_go_back){
            Intent intent = new Intent(UsersActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }


    // Send the correct list of users to the adapter
    private void fetchUserData() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                originalObjectUserList.clear();
                objectUserList.clear();

                boolean isAdmin = currentUsername.equals(ADMIN_USERNAME);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Object_User objectUser = snapshot.getValue(Object_User.class);

                    if (isAdmin || snapshot.getKey().equals(currentUsername)) {
                        objectUserList.add(objectUser);
                        originalObjectUserList.add(objectUser);
                    }
                }


                userAdapter = new UserAdapter(UsersActivity.this, objectUserList);
                listViewUsers.setAdapter(userAdapter);


                // Long click on the object will send to update the profile
                listViewUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        Object clickedItem = parent.getItemAtPosition(position);
                        Object_User clickedObjectUser = (Object_User) clickedItem;
                        String ClickedUsername = clickedObjectUser.getUsername();

                        if(currentUsername.equals(ClickedUsername)){
                            Intent intent = new Intent(UsersActivity.this, signUpActivity.class);
                            intent.putExtra("activity","update");
                            startActivity(intent);
                            finish();
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
        List<Object_User> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(originalObjectUserList);
        } else {
            for (Object_User objectUser : originalObjectUserList) {
                if (objectUser.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(objectUser);
                }
            }
        }
        objectUserList.clear();
        objectUserList.addAll(filteredList);

        userAdapter.notifyDataSetChanged();
    }


    private void sortUserListByAge() {
        Collections.sort(objectUserList, new Comparator<Object_User>() {
            @Override
            public int compare(Object_User objectUser1, Object_User objectUser2) {
                // Calculate ages for both users
                int age1 = calculateAge(objectUser1.getDateOfBirth());
                int age2 = calculateAge(objectUser2.getDateOfBirth());

                // Compare by age (ascending order)
                return Integer.compare(age1, age2); // Use Integer.compare for cleaner code
            }
        });
        userAdapter.notifyDataSetChanged();
    }

    private void sortUserListByUsername() {
        Collections.sort(objectUserList, new Comparator<Object_User>() {
            @Override
            public int compare(Object_User objectUser1, Object_User objectUser2) {
                return objectUser1.getUsername().compareToIgnoreCase(objectUser2.getUsername());
            }
        });
        userAdapter.notifyDataSetChanged();
    }

    private int calculateAge(String dateOfBirth) {
        // Parse the date of birth string into a Calendar object
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date dob = sdf.parse(dateOfBirth); // Convert string to Date
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);

            Calendar today = Calendar.getInstance(); // Get current date

            int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);

            // Adjust if birthday hasn't occurred yet this year
            if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age; // Return calculated age
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0; // Return 0 if parsing fails
    }







}