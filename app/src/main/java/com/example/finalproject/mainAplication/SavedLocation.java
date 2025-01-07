package com.example.finalproject.mainAplication;

public class SavedLocation {
    private String username;
    private double latitude;
    private double longitude;
    private String city;
    private String street;
    private int houseNum;

    public SavedLocation(){}


    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public double getLatitude() {return latitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude() {return longitude;}
    public void setLongitude(double longitude) {this.longitude = longitude;}

    public String getCity() {return city;}
    public void setCity(String city) {this.city = city;}

    public String getStreet() {return street;}
    public void setStreet(String street) {this.street = street;}

    public int getHouseNum() {return houseNum;}
    public void setHouseNum(int houseNum) {this.houseNum = houseNum;}
}
