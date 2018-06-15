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
public interface WordDao {
    @Query("SELECT * FROM word")
    LiveData<List<Word>> getAll();

    @Query("SELECT word_id FROM word WHERE word_name LIKE :word")
    int findIdByWord(String word);

    @Query("SELECT word_name FROM word WHERE word_id LIKE :wordId")
    String findWordById(int wordId);

    @Query("SELECT * FROM word")
    List<Word> getAllList();

    @Query("SELECT word_id FROM word WHERE word_name LIKE :substring")
    List<Integer> findIdsForWordsContainingSubstring(String substring);

    @Insert
    void insertAll(Word... words);

    @Delete
    void delete(Word word);

    @Update
    void update(Word... words);
}
