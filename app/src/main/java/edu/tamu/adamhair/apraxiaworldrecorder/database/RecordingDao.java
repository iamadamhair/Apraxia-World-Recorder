package edu.tamu.adamhair.apraxiaworldrecorder.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adamhair on 4/9/2018.
 */

@Dao
public interface RecordingDao {
    @Query("SELECT * FROM recording")
    LiveData<List<Recording>> getAll();

    @Query("SELECT * FROM recording WHERE user_id LIKE :userId")
    LiveData<List<Recording>> findByUserId(int userId);

    @Query("SELECT * FROM recording WHERE user_id LIKE :userId")
    List<Recording> findListByUserId(int userId);

    @Query("SELECT * FROM recording WHERE user_id LIKE :userId AND word_id LIKE :wordId")
    LiveData<List<Recording>> findByUserIdAndWordId(int userId, int wordId);

    @Query("SELECT * FROM recording WHERE user_id LIKE :userId AND word_id LIKE :wordId")
    List<Recording> findListByUserIdAndWordId(int userId, int wordId);

    @Query("SELECT * FROM recording WHERE recording_id LIKE :recordingId")
    Recording findByRecordingId(int recordingId);

    @Insert
    void insertAll(Recording... recordings);

    @Delete
    void delete(Recording recording);

    @Update
    void update(Recording... recordings);
}
