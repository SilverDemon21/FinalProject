package com.example.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class User_Profile extends AppCompatActivity {
    private Button  deleteProfile, BtnUpdate;
    private TextView showEmail;
    private ImageButton btnMainMenu;
    private EditText EtUpdateName, EtUpdatePassword, EtUpdateEmail, EtUpdatePhone;
    sharedPref_manager manager;
    FirebaseDatabase database;
    DatabaseReference reference;
    private ImageButton btnBackToMain;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        deleteProfile = findViewById(R.id.deleteProfile);
        EtUpdateEmail = findViewById(R.id.EtUpdateEmail);
        EtUpdatePassword = findViewById(R.id.EtUpdatePassword);
        EtUpdateName = findViewById(R.id.EtUpdateName);
        BtnUpdate = findViewById(R.id.BtnUpdate);
        btnBackToMain = findViewById(R.id.btnBackToMain);
        EtUpdatePhone = findViewById(R.id.EtUpdatePhone);


        info_validation validation;

        manager = new sharedPref_manager(User_Profile.this, "LoginUpdate");


        // set defult image
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.drawable.img_defult_user_image);

            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }

        // button to go back to main activity
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(User_Profile.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // button to delete users profile
        deleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlertDelete();
            }
        });

        // button to update users profile
        BtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {UpdateProfile();}
        });
    }




    // checks all the input data - if good sending false
    public boolean checkNewProfile(){
        boolean WrongNewProfile = false;
        String email = EtUpdateEmail.getText().toString().trim();
        String name = EtUpdateName.getText().toString().trim();
        String password = EtUpdatePassword.getText().toString().trim();
        String phone = EtUpdatePhone.getText().toString().trim();
        if(!email.isEmpty()){
            if(!info_validation.email_validation(email)){
                WrongNewProfile = true;
                EtUpdateEmail.setError("Pls write valid email / email is already exists");
            }
        }
        if(!name.isEmpty()){
            if(!info_validation.name_validation(name)){
                WrongNewProfile = true;
                EtUpdateName.setError("The name should be between 2 - 10 characters");
            }
        }
        if(!password.isEmpty()){
            if(!info_validation.password_validation(password)){
                WrongNewProfile = true;
                EtUpdateName.setError("The password should be between 6 - 15 characters");
            }
        }
        if(!phone.isEmpty()) {
            if (!info_validation.phoneNumber_validation(phone)) {
                WrongNewProfile = true;
                EtUpdatePhone.setError("Pls write a real phone / this phone is already exists");
            }
        }
        return WrongNewProfile;
    }


    // Updates the profile if the stats are validated
    public void UpdateProfile(){

        if (!checkNewProfile()){
            String currentUser = manager.getUsername();
            String email = EtUpdateEmail.getText().toString().trim();
            String name = EtUpdateName.getText().toString().trim();
            String password = EtUpdatePassword.getText().toString().trim();
            String phone = EtUpdatePhone.getText().toString().trim();
            Map<String, Object> updates = new HashMap<>();
            if (!email.isEmpty()){
                updates.put("email", email);
                manager.setEmail(email);
            }
            if(!name.isEmpty()){
                updates.put("name", name);
                manager.setName(name);
            }
            if(!password.isEmpty()){
                updates.put("password", password);
            }
            if (!phone.isEmpty()){
                updates.put("phoneNum", phone);
            }
            reference = FirebaseDatabase.getInstance().getReference("users").child(currentUser);
            reference.updateChildren(updates);

        }
    }



    public void createAlertDelete(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deleting the current profile");
        builder.setMessage("Are you sure?");
        builder.setCancelable(false);
        builder.setPositiveButton("Agree", new DeleteProfile());
        builder.setNegativeButton("Disagree", new DeleteProfile());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
    }


    private class DeleteProfile implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if(i == -1){
                sharedPref_manager manager = new sharedPref_manager(User_Profile.this,"LoginUpdate");
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                userRef.orderByChild("username").equalTo(manager.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DataSnapshot userSnap = snapshot.getChildren().iterator().next();
                        userSnap.getRef().removeValue();
                        manager.convertToLoggedOut();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        }
    }
}