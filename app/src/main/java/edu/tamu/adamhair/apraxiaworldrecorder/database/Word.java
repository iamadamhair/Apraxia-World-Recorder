package edu.tamu.adamhair.apraxiaworldrecorder.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by adamhair on 4/9/2018.
 */

@Entity
public class Word {
    @PrimaryKey(autoGenerate = true)
    private int word_id;

    @ColumnInfo(name = "word_name")
    private String wordName;

    public Word(String wordName) {
        this.wordName = wordName;
    }

    public int getWord_id() {
        return word_id;
    }

    public void setWord_id(int word_id) {
        this.word_id = word_id;
    }

    public String getWordName() {
        return wordName;
    }

    public void setWordName(String wordName) {
        this.wordName = wordName;
    }
}
