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
public interface WordDao {
    @Query("SELECT * FROM word")
    List<Word> getAll();

    @Query("SELECT * FROM word WHERE word_name LIKE :word")
    Word findByWord(String word);

    @Insert
    void insertAll(Word... words);

    @Delete
    void delete(Word word);
}
