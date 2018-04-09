package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by adamhair on 4/9/2018.
 */

@Dao
public interface RepetitionDao {
    @Query("SELECT * FROM repetition")
    List<Repetition> getAll();

    @Query("SELECT * FROM repetition WHERE user_id LIKE :userId AND word_id LIKE :wordId")
    Repetition findByUserIdAndWordId(int userId, int wordId);

    @Insert
    void insertAll(Repetition... repetitions);

    @Delete
    void delete(Repetition repetition);
}
