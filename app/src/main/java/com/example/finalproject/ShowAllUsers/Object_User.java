package com.example.finalproject.ShowAllUsers;

public class Object_User {
    private String email;
    private String name;
    private String username;
    private String phoneNum;
    private String photoUrl;
    private String dateOfBirth;

    public Object_User() {}

    public Object_User(String email, String name, String username, String phone, String photoUrl, String dateOfBirth) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.phoneNum = phone;
        this.photoUrl = photoUrl;
        this.dateOfBirth = dateOfBirth;
    }



    public String getEmail() {return email;}

    public String getName() {return name;}

    public String getUsername() {return username;}

    public String getPhoneNum() {return phoneNum;}

    public String getPhotoUrl() {return photoUrl;}

    public String getDateOfBirth() {return dateOfBirth;}
}