package edu.tamu.adamhair.apraxiaworldrecorder.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by adamhair on 8/16/2018.
 */

@Entity
public class Probe {
    @PrimaryKey(autoGenerate = true)
    private int probe_id;

	@ColumnInfo(name = "probe_number")
    private int probeNumber;
	
    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "file_location")
    private String fileLocation;

    @ColumnInfo(name = "word_id")
    private int wordId;

	@ColumnInfo(name = "word_name")
	private String wordName;
	
    @ColumnInfo(name = "is_correct")
    private boolean isCorrect;

    @ColumnInfo(name = "probe_date")
	private String probeDate;
	
	public Probe(int probeNumber, int userId, String fileLocation, int wordId, String wordName, boolean isCorrect, String probeDate) {
		this.probeNumber = probeNumber;
		this.userId = userId;
		this.fileLocation = fileLocation;
		this.wordId = wordId;
		this.wordName = wordName;
		this.isCorrect = isCorrect;
		this.probeDate = probeDate;
	}
	
	public int getProbe_id() {
		return probe_id;
	}
	
	public void setProbe_id(int probe_id) {
		this.probe_id = probe_id;
	}
	
	public int getProbeNumber() {
		return probeNumber;
	}
	
	public void setProbeNumber(int probeNumber) {
		this.probeNumber = probeNumber;
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
	
	public String getWordName() {
		return wordName;
	}
	
	public void setWordName(String wordName) {
		this.wordName = wordName;
	}
	
	public boolean isCorrect() {
		return isCorrect;
	}
	
	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}
	
	public String getProbeDate() {
		return probeDate;
	}
	
	public void setProbeDate(String probeDate) {
		this.probeDate = probeDate;
	}
}