package com.example.finalproject.mainAplication;

public class SavedLocation {
    private String username;
    private double latitude;
    private double longitude;
    private String address;

    public SavedLocation(){}


    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public double getLatitude() {return latitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude() {return longitude;}
    public void setLongitude(double longitude) {this.longitude = longitude;}

    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}
}
