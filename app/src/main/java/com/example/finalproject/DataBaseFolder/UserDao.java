package com.example.finalproject.DataBaseFolder;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM tblUsers")
    List<User> getAll();
    @Query("SELECT * FROM tblUsers WHERE userId == (:userId)")
    User getUserByUserId (long userId);

    @Insert
    long insert(User user);
    @Update
    void update(User user);

    @Delete
    void delete(User user);

}
