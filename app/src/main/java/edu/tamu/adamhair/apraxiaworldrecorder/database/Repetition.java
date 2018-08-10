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

    @ColumnInfo(name = "asr_tested")
    private boolean asrTested;

    @ColumnInfo(name = "should_export")
    private boolean shouldExport;

    public Repetition(int userId, String wordName, int wordId, int numCorrect, int numIncorrect, boolean shouldExport, boolean asrTested) {
        this.userId = userId;
        this.wordId = wordId;
        this.wordName = wordName;
        this.numCorrect = numCorrect;
        this.numIncorrect = numIncorrect;
        this.shouldExport = shouldExport;
        this.asrTested = asrTested;
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

    public boolean shouldExport() {
        return shouldExport;
    }

    public void setShouldExport(boolean shouldExport) {
        this.shouldExport = shouldExport;
    }

    public boolean asrTested() {return asrTested;}

    public void setAsrTested(boolean asrTested) {this.asrTested = asrTested;}
}
