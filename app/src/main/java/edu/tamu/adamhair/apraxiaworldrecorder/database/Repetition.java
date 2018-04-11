package edu.tamu.adamhair.apraxiaworldrecorder.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by adamhair on 4/9/2018.
 */

@Entity
public class Repetition {
    @PrimaryKey(autoGenerate = true)
    private int repetition_id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "word_id")
    private int wordId;

    @ColumnInfo(name = "word_name")
    private String wordName;

    @ColumnInfo(name = "num_correct")
    private int numCorrect;

    @ColumnInfo(name = "num_incorrect")
    private int numIncorrect;

    public Repetition(int userId, String wordName, int wordId, int numCorrect, int numIncorrect) {
        this.userId = userId;
        this.wordId = wordId;
        this.wordName = wordName;
        this.numCorrect = numCorrect;
        this.numIncorrect = numIncorrect;
    }

    public int getRepetition_id() {
        return repetition_id;
    }

    public void setRepetition_id(int repetition_id) {
        this.repetition_id = repetition_id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getWordName() {
        return wordName;
    }

    public void setWordName(String wordName) {
        this.wordName = wordName;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public int getNumCorrect() {
        return numCorrect;
    }

    public void setNumCorrect(int numCorrect) {
        this.numCorrect = numCorrect;
    }

    public int getNumIncorrect() {
        return numIncorrect;
    }

    public void setNumIncorrect(int numIncorrect) {
        this.numIncorrect = numIncorrect;
    }
}
