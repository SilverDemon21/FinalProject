package com.example.finalproject.RegestrationXLogin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.sharedPref_manager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {

    EditText login_username, login_password;
    Button login_button;
    TextView signUpRedirectText,sh;
    ImageButton backMainActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        // implements all the variables
        login_password = findViewById(R.id.login_password);
        login_username = findViewById(R.id.login_username);
        login_button = findViewById(R.id.login_button);
        signUpRedirectText = findViewById(R.id.signUpRedirectText);
        backMainActivity = findViewById(R.id.backMainActivity);

        // go back to main activity button
        backMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // login button
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validatePassword() | !validateUserName()){

                }
                else{
                    checkUser();
                }
            }
        });

        // go to register activity button
        signUpRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loginActivity.this, signUpActivity.class);
                intent.putExtra("activity", "create");
                startActivity(intent);
            }
        });
    }

    //  validate username for login
    public Boolean validateUserName(){
        String val = login_username.getText().toString();

        if(val.isEmpty()){
            login_username.setError("UserName cannot be empty");
            return false;
        }
        else{
            login_username.setError(null);
            return true;
        }
    }
    //  validate password for login
    public Boolean validatePassword(){
        String val = login_password.getText().toString();
        if(val.isEmpty()){
            login_password.setError("Password cannot be empty");
            return false;
        }
        else{
            login_password.setError(null);
            return true;
        }
    }



    // help function for calling all data savings
    public void saveData()
    {
        saveDataAtSharePreferences(login_username.getText().toString().trim());
    }
    // data saving for login method
    private void saveDataAtSharePreferences(String username) {
        sharedPref_manager manager = new sharedPref_manager(loginActivity.this, "LoginUpdate");
        manager.setUsername(username);
        manager.saveEmail(username);
        manager.saveName(username);
        manager.savePhone(username);
        manager.savePhotoUrl(username);
        manager.convertToLoggedIn();
    }


    // login checking and firebase validation
    public void checkUser(){
        String userUsername = login_username.getText().toString().trim();
        String userPassword = login_password.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);


        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    login_username.setError(null);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);

                    if (passwordFromDB.equals(userPassword)){
                        login_username.setError(null);
                        Intent intent = new Intent(loginActivity.this, MainActivity.class);
                        saveData();
                        startActivity(intent);
                    }
                    else{
                        login_password.setError("invalid credentials");
                        login_password.requestFocus();
                    }
                }
                else {
                    login_username.setError("User does not exist");
                    login_username.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}