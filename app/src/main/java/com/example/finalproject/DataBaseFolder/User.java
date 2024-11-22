package com.example.finalproject.DataBaseFolder;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.sql.Date;

@Entity(tableName = "tblUsers")
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "userId")
    private long userId;

    @ColumnInfo(name = "userName")
    private String userName;

    @ColumnInfo(name = "userWeight")
    private double userWeigt;

    @ColumnInfo(name = "userAcademic")
    private boolean userAcademic;

   @ColumnInfo(name = "userBirthDay")
   @TypeConverters(Converters.class)
   private Date userBirthDay;

    public User(){}

    public Date getUserBirthDay() {
        return userBirthDay;
    }

    public void setUserBirthDay(Date userBirthDay) {
        this.userBirthDay = userBirthDay;
    }

    public boolean isUserAcademic() {
        return userAcademic;
    }

    public void setUserAcademic(boolean userAcademic) {
        this.userAcademic = userAcademic;
    }

    public double getUserWeigt() {
        return userWeigt;
    }

    public void setUserWeigt(double userWeigt) {
        this.userWeigt = userWeigt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}

