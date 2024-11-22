package com.example.finalproject;

public class User {
    private String email;
    private String name;
    private String username;
    private String phoneNum;
    private String photoName;

    public User() {}

    public User( String email, String name, String username, String phone, String photoName) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.phoneNum = phone;
        this.photoName = photoName;
    }



    public String getEmail() {return email;}

    public String getName() {return name;}

    public String getUsername() {return username;}

    public String getPhoneNum() {return phoneNum;}

    public String getPhotoName() {return photoName;}
}