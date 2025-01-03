package com.example.finalproject.RegestrationXLogin;


import android.util.Patterns;

import com.google.firebase.database.DatabaseReference;



public class info_validation {
    private static DatabaseReference mDatabase;

    private static final String nameRegex = "^[a-zA-Z\\s]+$";
    private static final String usernameRegex = "^[a-zA-Z0-9-,]+$";
    private static final String passwordRegex = "^[a-zA-Z0-9!&*()^%]+$";
    private static final String emailRegex = "^[a-zA-Z0-9!&*()^%@.]+$";
    private static final String phoneRegex = "^[0-9]{10}$";

    // username validation
    public static boolean username_validation(String username) {
        int leng = username.length();
        if (leng > 15 | leng < 5 | !username.matches(usernameRegex)) {
            return false;
        } else {
            return true;
        }
    }

    // email validation - bug with sync
    public static boolean email_validation(String email) {
        if (email.isEmpty() | !Patterns.EMAIL_ADDRESS.matcher(email).matches() | !email.matches(emailRegex)) {
            return false;
        }
        return true;
    }

    // name validation
    public static boolean name_validation(String name) {
        int leng = name.length();
        if (leng > 10 | leng < 2 | !name.matches(nameRegex)) {
            return false;
        }
        return true;
    }

    //password validation
    public static boolean password_validation(String password) {
        int leng = password.length();
        if (leng > 18 | leng < 6 | !password.matches(passwordRegex)) {
            return false;
        }
        return true;
    }

    // phone number validation
    public static boolean phoneNumber_validation(String phone) {
        if (!phone.matches(phoneRegex)) {
            return false;
        }
        return true;
    }

}


