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
public interface RepetitionDao {
    @Query("SELECT * FROM repetition")
    LiveData<List<Repetition>> getAll();

    @Query("SELECT * FROM repetition WHERE user_id LIKE :userId")
    List<Repetition> getAllOfAUserList(int userId);

    @Query("SELECT * FROM repetition WHERE user_id LIKE :userId AND word_id LIKE :wordId")
    LiveData<List<Repetition>> findByUserIdAndWordId(int userId, int wordId);

    @Query("SELECT * FROM repetition WHERE user_id LIKE :userId AND word_id LIKE :wordId")
    Repetition findRepetitionByUserIdAndWordId(int userId, int wordId);

    @Query("SELECT * FROM repetition WHERE user_id LIKE :userId")
    LiveData<List<Repetition>> findByUserId(int userId);

    @Query("SELECT * FROM repetition WHERE user_id LIKE :userId ORDER BY word_name ASC")
    LiveData<List<Repetition>> findByUserIdSorted(int userId);

    @Query("SELECT * FROM repetition WHERE user_id LIKE :userId AND word_id IN (:wordIds) ORDER BY word_name ASC")
    List<Repetition> findListByUserIdAndWordIdSorted(int userId, List<Integer> wordIds);

    @Insert
    void insertAll(Repetition... repetitions);

    @Delete
    void delete(Repetition repetition);

    @Update
    void update(Repetition... repetitions);
}
