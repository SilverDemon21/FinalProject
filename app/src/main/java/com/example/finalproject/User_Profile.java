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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class User_Profile extends AppCompatActivity {
    private Button  deleteProfile;
    private TextView showEmail;
    sharedPref_manager manager;
    FirebaseDatabase database;
    DatabaseReference reference;
    private ImageButton btnBackToMain;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        deleteProfile = findViewById(R.id.deleteProfile);
        btnBackToMain = findViewById(R.id.btnBackToMain);
        manager = new sharedPref_manager(User_Profile.this, "LoginUpdate");

        if (!manager.getIsLoggedIn()){
            deleteProfile.setVisibility(View.GONE);
        }

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
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                databaseReference.get().addOnCompleteListener(task -> {
                    String username = manager.getUsername();
                    String email = manager.getEmail();
                    String phone = manager.getPhoneNum();

                    databaseReference.child("users").child(username).removeValue();
                    databaseReference.child("emails").child(email).removeValue();
                    databaseReference.child("phoneNumbers").child(phone).removeValue();
                    manager.convertToLoggedOut();

                    StorageReference storageRef = FirebaseStorage.getInstance("gs://final-project-be550.firebasestorage.app").getReference();
                    StorageReference imageRef = storageRef.child(username);

                    imageRef.delete();

                });

            }
        }
    }
}