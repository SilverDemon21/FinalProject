package com.example.finalproject.RegestrationXLogin;

public class HellperSignUpClass {
    String name, email, username, password, phoneNum, photoUrl, dateOfBirth;

    public HellperSignUpClass(String name, String email, String username, String password, String phoneNum, String photoUrl, String dateOfBirth) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.phoneNum = phoneNum;
        this.photoUrl = photoUrl;
        this.dateOfBirth = dateOfBirth;
    }
    public HellperSignUpClass(){

    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNum(){return phoneNum;}
    public void setPhoneNum(String phoneNum) {this.phoneNum = phoneNum; }

    public String getPhotoUrl() {return photoUrl;}
    public void setPhotoUrl(String photoUrl) {this.photoUrl = photoUrl;}

    public String getDateOfBirth() {return dateOfBirth;}
    public void setDateOfBirth(String dateOfBirth) {this.dateOfBirth = dateOfBirth;}


}
