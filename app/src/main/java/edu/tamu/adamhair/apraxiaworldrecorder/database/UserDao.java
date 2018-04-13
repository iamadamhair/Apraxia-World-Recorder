package edu.tamu.adamhair.apraxiaworldrecorder.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by adamhair on 4/9/2018.
 */

@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM user ORDER BY username ASC")
    LiveData<List<User>> getAllSorted();

    @Query("SELECT username FROM user")
    LiveData<List<String>> getAllUsernames();

    @Query("SELECT * FROM user WHERE username LIKE :username")
    User findByUsername(String username);

    @Query("SELECT username FROM user ORDER BY username ASC")
    LiveData<List<String>> getAllUsernamesSorted();

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);

    @Update
    void update(User... users);
}