package com.example.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.example.finalproject.RegestrationXLogin.loginActivity;
import com.example.finalproject.RegestrationXLogin.signUpActivity;
import com.example.finalproject.ShowAllUsers.UsersActivity;
import com.example.finalproject.mainAplication.ListUserGroups;
import com.example.finalproject.mainAplication.ServiceUserLocation;
import com.example.finalproject.mainAplication.mapAndLogic;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private CircleImageView userImage;
    private TextView sharedUser;
    private sharedPref_manager manager;
    private TextView userConnection;
    private IsUserConnectedToInternetThread Thread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedUser = findViewById(R.id.sharedUser);
        manager =  new sharedPref_manager(MainActivity.this, "LoginUpdate");
        userImage = findViewById(R.id.userImage);

        userConnection = findViewById(R.id.userConnection);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBarHome);

        bottomNavigationView.setSelectedItemId(R.id.menu_home);


        bottomNavigationView.setOnItemSelectedListener(item ->{
            if(item.getItemId() == R.id.menu_home){
                return true;
            }
            else if(item.getItemId() == R.id.menu_map){
                if(manager.getIsLoggedIn()){
                    startActivity(new Intent(getApplicationContext(), mapAndLogic.class));
                    overridePendingTransition(0, 0);
                }
                else{
                    Toast.makeText(MainActivity.this, "First Log In", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            else if(item.getItemId() == R.id.menu_profile){
                startActivity(new Intent(getApplicationContext(), User_Profile.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if(item.getItemId() == R.id.menu_groups){
                if(manager.getIsLoggedIn()){
                    startActivity(new Intent(getApplicationContext(), ListUserGroups.class));
                    overridePendingTransition(0, 0);
                }
                else{
                    Toast.makeText(MainActivity.this, "First Log In", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return true;


        });
        // updating the main activity considering the sharedPref
        updateTitle();


       Thread = new IsUserConnectedToInternetThread(this, new IsUserConnectedToInternetThread.InternetStatusListener() {
            @Override
            public void onInternetStatusChanged(boolean isConnected) {
                runOnUiThread(() -> {
                    userConnection.setText(isConnected ? "Connected" : "disconnected");
                    if(isConnected){
                        userConnection.setBackgroundResource(R.drawable.background_person_connected);
                    }
                    else{
                        userConnection.setBackgroundResource(R.drawable.background_person_disconnected);
                    }
                });
            }
        });
    Thread.startChecking();

    }


    // update title
    private void updateTitle() {
        if (!manager.getIsLoggedIn()) {
            sharedUser.setText("Welcome user");
            userImage.setImageResource(R.drawable.defult_user_image);
        } else if(manager.getUsername().equals("admin")) {
            sharedUser.setText("Welcome, " + manager.getUsername() + " Admin");
        } else{
            sharedUser.setText("Welcome, " + manager.getUsername());
        }
        if(manager.getIsLoggedIn()){
            displayImage(manager.getPhotoUrl());
        }
    }

    private void displayImage(String photoUrl){

        Glide.with(this)
                .load(photoUrl)
                .into(userImage);

    }



    //creates pointer for the xml file of menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (manager.getIsLoggedIn()) {
            getMenuInflater().inflate(R.menu.menu_logged_in, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_logged_out, menu);
        }
        return true;
    }


    //invalidate the right menu and do the following things
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_logIn) {
            Intent intent = new Intent(MainActivity.this, loginActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.menu_logout) {
            createAlertSignOut();
        }
        else if(item.getItemId() == R.id.menu_signUp){
            Intent intent = new Intent(MainActivity.this, signUpActivity.class);
            intent.putExtra("activity", "create");
            startActivity(intent);

        } else if (item.getItemId() == R.id.menu_userInfo) {
            Intent intent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.menu_updateProfile){
            Intent intent = new Intent(MainActivity.this, signUpActivity.class);
            intent.putExtra("activity", "update");
            startActivity(intent);
        }
        else if (item.getItemId()==R.id.menu_about) {
            AboutAppDialog.showAboutDialog(this);
        }
        return true;
    }


    // alert for signing out
    public void createAlertSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signing out of profile");
        builder.setMessage("Are you sure?");
        builder.setCancelable(false);
        builder.setPositiveButton("Agree", new SignOut());
        builder.setNegativeButton("Disagree", new SignOut());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
    }

    // interface that implements the sign out alert
    private class SignOut implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i){
            if(i == -1){
                manager.convertToLoggedOut();
                updateTitle();
                invalidateOptionsMenu();
                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent serviceIntent = new Intent(MainActivity.this, ServiceUserLocation.class);
                stopService(serviceIntent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Thread.stopChecking();
    }
}

