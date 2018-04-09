package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by adamhair on 4/9/2018.
 */

@Entity
public class Recording {
    @PrimaryKey
    private int recording_id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "file_location")
    private String fileLocation;

    @ColumnInfo(name = "word_id")
    private int wordId;

    @ColumnInfo(name = "is_correct")
    private boolean isCorrect;

    @ColumnInfo(name = "repetition_number")
    private int repetitionNumber;

    public int getRecording_id() {
        return recording_id;
    }

    public void setRecording_id(int recording_id) {
        this.recording_id = recording_id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getRepetitionNumber() {
        return repetitionNumber;
    }

    public void setRepetitionNumber(int repetitionNumber) {
        this.repetitionNumber = repetitionNumber;
    }
}
