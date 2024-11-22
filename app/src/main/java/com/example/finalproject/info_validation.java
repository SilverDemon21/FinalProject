package com.example.finalproject;

import android.telephony.PhoneNumberUtils;
import android.util.Patterns;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.concurrent.CountDownLatch;


public class info_validation {
    private static DatabaseReference mDatabase;


    // username validation
    public static boolean username_validation(String username){
        int leng = username.length();
        if (leng > 15 | leng < 5 | username.contains("'")){
            return false;
        }
        else{
            return true;
        }
    }

    // email validation - bug with sync
    public static boolean email_validation(String email){
        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        }
        return false;
    }

    // name validation
    public static boolean name_validation(String name){
        int leng = name.length();
        if (leng > 10 | leng < 2 | name.contains(".") | name.contains("'") | name.contains(",")){
            return false;
        }
        return true;
    }

    //password validation
    public static boolean password_validation(String password){
        int leng = password.length();
        if (leng > 18 | leng < 6){
            return false;
        }
        return true;
    }

    // phone number validation
    public static boolean phoneNumber_validation(String phone){
        if (PhoneNumberUtils.isGlobalPhoneNumber(phone)){
            return true;
        }
        return false;
    }
}


//mDatabase = FirebaseDatabase.getInstance().getReference("users");
//emailSnap = true;
//        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//    @Override
//    public void onDataChange(@NonNull DataSnapshot snapshot) {
//        for(DataSnapshot usersnap : snapshot.getChildren()){
//            String temp = usersnap.child("email").getValue(String.class);
//            if (temp.equals(email)){
//                emailSnap = false;
//                break;
//            }
//        }
//    }
//
//    @Override
//    public void onCancelled(@NonNull DatabaseError error) {
//
//    }
//});
