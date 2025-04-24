package com.example.finalproject.RegestrationXLogin;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.MainActivity;
import com.example.finalproject.Permission;
import com.example.finalproject.R;
import com.example.finalproject.sharedPref_manager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Random;

public class loginActivity extends AppCompatActivity {

    EditText login_username, login_password;
    Button login_button;
    TextView signUpRedirectText,sh,btnForgotPassword;
    ImageButton backMainActivity;

    private String recoveryUsername;


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
        btnForgotPassword = findViewById(R.id.btnForgotPassword);

        // go back to main activity button
        backMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
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

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(loginActivity.this);
                View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                EditText etRecoveryPhoneNumber = dialogView.findViewById(R.id.etRecoveryPhoneNumber);
                Button btnSendSmsWithCode = dialogView.findViewById(R.id.btnSendSmsWithCode);
                Button btnRecoverPasswordWithCode = dialogView.findViewById(R.id.btnRecoverPasswordWithCode);
                EditText etRecoveryCodeFromSms = dialogView.findViewById(R.id.etRecoveryCodeFromSms);

                btnSendSmsWithCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference phoneRef = FirebaseDatabase.getInstance().getReference().child("phoneNumbers");
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
                        DatabaseReference recoveryCodeRef = FirebaseDatabase.getInstance().getReference().child("recoveryCodes");

                        String recoveryPhone = etRecoveryPhoneNumber.getText().toString().trim();
                        if(!recoveryPhone.isEmpty()){

                            phoneRef.child(recoveryPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        Random rnd = new Random();
                                        if(Permission.DoesUserHasAllOfThePermissions(loginActivity.this)){
                                            String usernameOfRecoveryPhone = snapshot.getValue(String.class);

                                            userRef.child(usernameOfRecoveryPhone).child("RecoveryCode").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.getValue(String.class) != null){
                                                        recoveryCodeRef.child(snapshot.getValue(String.class)).removeValue();
                                                    }


                                                    int generatedItem = rnd.nextInt(10000-10+1) + 10;
                                                    SmsManager sms = SmsManager.getDefault();
                                                    sms.sendTextMessage(etRecoveryPhoneNumber.getText().toString().trim(), null, String.valueOf(generatedItem), null, null);
                                                    Toast.makeText(loginActivity.this, "Recovery code was sent to your sms", Toast.LENGTH_SHORT).show();



                                                    HashMap<String, Object> updates = new HashMap<>();
                                                    updates.put(String.valueOf(generatedItem), usernameOfRecoveryPhone);
                                                    recoveryCodeRef.updateChildren(updates);

                                                    userRef.child(usernameOfRecoveryPhone).child("RecoveryCode").setValue(String.valueOf(generatedItem));
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                        else{
                                            Toast.makeText(loginActivity.this, "there is no no permmisions", Toast.LENGTH_SHORT).show();
                                            Permission.GrantAllPermissions(loginActivity.this);
                                        }
                                    }
                                    else{
                                        Toast.makeText(loginActivity.this, "There is no such number at the database", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        else{
                            Toast.makeText(loginActivity.this, "pls enter a phone number", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                btnRecoverPasswordWithCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       if(etRecoveryCodeFromSms.getText().toString().trim().isEmpty()){
                           Toast.makeText(loginActivity.this, "Pls enter your recovery code", Toast.LENGTH_SHORT).show();
                       }
                       else{
                           dialog.dismiss();
                           DatabaseReference recoveryCodeRef = FirebaseDatabase.getInstance().getReference().child("recoveryCodes");
                           DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
                           recoveryCodeRef.child(etRecoveryCodeFromSms.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   if(!snapshot.exists()){
                                       Toast.makeText(loginActivity.this, "There is no such recovery code", Toast.LENGTH_SHORT).show();
                                   }
                                   else{
                                       recoveryUsername = snapshot.getValue(String.class);
                                       LayoutInflater inflater = LayoutInflater.from(loginActivity.this);
                                       View dialogView = inflater.inflate(R.layout.dialog_update_password, null);

                                       AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity.this);
                                       builder.setView(dialogView);
                                       AlertDialog dialog = builder.create();

                                       EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
                                       EditText etNewPasswordConfirm = dialogView.findViewById(R.id.etNewPasswordConfirm);
                                       Button btnUpdatePassword = dialogView.findViewById(R.id.btnUpdatePassword);

                                        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if(InfoValidation.password_validation(etNewPassword.getText().toString().trim())){
                                                    if(etNewPasswordConfirm.getText().toString().trim().equals(etNewPassword.getText().toString().trim())){

                                                        userRef.child(recoveryUsername).child("password").setValue(etNewPassword.getText().toString().trim());
                                                        dialog.dismiss();
                                                    }
                                                    else{
                                                        etNewPasswordConfirm.setError("The password does not match the confirm password");
                                                    }
                                                }
                                                else{
                                                    etNewPassword.setError("The password should be at least 6 characters");
                                                }
                                            }
                                        });

                                       recoveryCodeRef.child(etRecoveryCodeFromSms.getText().toString().trim()).removeValue();
                                       userRef.child(recoveryUsername).child("RecoveryCode").removeValue();
                                       dialog.show();


                                   }

                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {

                               }
                           });

                       }
                    }
                });

                dialog.show();
            }
        });

        // go to register activity button
        signUpRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loginActivity.this, signUpActivity.class);
                intent.putExtra("activity", "create");
                startActivity(intent);
                finish();
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
    // rework
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
                        finish();
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