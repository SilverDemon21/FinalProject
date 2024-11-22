package com.example.finalproject;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class sharedPref_manager {

    private boolean isLoggedIn;
    private String username;
    private String email;
    private String name;
    private String phoneNum;
    private String photoName;
    SharedPreferences sh;
    SharedPreferences.Editor editor;
    Context context;


    public sharedPref_manager(Context context, String factory){
        this.context = context;
        sh = context.getSharedPreferences(factory,context.MODE_PRIVATE);
        this.isLoggedIn = false;
        editor = sh.edit();
    }

    public void setName(String name){editor.putString("name",name); editor.commit();}
    public String getName(){return sh.getString("name", "");}

    public void setEmail(String email){editor.putString("email", email); editor.commit();}
    public String getEmail(){return sh.getString("email", "");}

    public void setUsername(String username){editor.putString("UserName", username); editor.commit();}
    public String getUsername(){return sh.getString("UserName","");}

    public void setPhoneNum(String phone){editor.putString("phone", phone); editor.commit();}
    public String getPhoneNum(){return sh.getString("phone", "");}

    public boolean getIsLoggedIn() {return sh.getBoolean("isLoggedIn",false);}
    public void setIsLoggedIn(boolean loggedIn) {editor.putBoolean("isLooggedIn", loggedIn); editor.commit();}

    public String getPhotoName() {return sh.getString("photoName", "");}
    public void setPhotoName(String photoName) {editor.putString("photoName", photoName); editor.commit();}


    public void convertToLoggedIn(){
        editor.putBoolean("isLoggedIn", true);
        editor.commit();
    }

    public void convertToLoggedOut(){
        editor.putString("UserName", "");
        editor.putBoolean("isLoggedIn", false);
        editor.putString("email", "");
        editor.putString("name", "");
        editor.putString("phone", "");
        editor.putString("photoName", "");
        editor.commit();
    }


    public void saveEmail(String username){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email = snapshot.child("email").getValue(String.class);
                editor.putString("email", email);
                editor.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void saveName(String username){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name= snapshot.child("name").getValue(String.class);
                editor.putString("name", name);
                editor.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void savePhone(String username){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                phoneNum = snapshot.child("phoneNum").getValue(String.class);
                editor.putString("phone", phoneNum);
                editor.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void savePhotoName(String username){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                photoName = snapshot.child("photoName").getValue(String.class);
                editor.putString("photoName", photoName);
                editor.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
