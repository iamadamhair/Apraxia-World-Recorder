package edu.tamu.adamhair.apraxiaworldrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Word;

public class FileManager {

    /*

    TO DO:
    Make sure file system is correctly configured for Apraxia World

    Future:
    Read file system if it exists, update DB to match

    File Hierarchy:
    Android Root
        Apraxia World Audio
            users.dat
            username
                words.dat
                Calibration
                    labels.dat
                    wordName
                        rep1.wav
                        ...
                Game Recordings
                    TBD
    */

    public static boolean awFolderExists() {
        return getAwFolder().isDirectory();
    }

    public static void createAwFolder() {
        getAwFolder().mkdir();
    }

    public static void createUserFolder(String username) {
        File userFolder = new File(getAwFolder() + "/" + username);
        File calibrationFolder = new File(userFolder + "/Calibration");
        File gameRecordingsFolder = new File(userFolder + "/Game Recordings");
        userFolder.mkdir();
        calibrationFolder.mkdir();
        gameRecordingsFolder.mkdir();
    }

    public static boolean userFolderExists(String username) {
        File userFolder = new File(getAwFolder(), username);
        return userFolder.isDirectory();
    }

    public static boolean checkAndRequestPermissions(Activity activity) {
        int request_response = 0;
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, request_response);
            return false;
        }
        return true;
    }

    public static boolean wordFolderExists(String username, String word) {
        File wordFolder = new File(getUserFolderString(username), "Calibration/" + word);
        return wordFolder.isDirectory();
    }

    public static void createWordFolder(String username, String word) {
        File wordFolder = new File(getUserFolderString(username), "Calibration/" +  word);
        wordFolder.mkdir();
    }

    public static void recreateUserDatFile(List<String> usernames, Context context) {
        File userDat = new File(getAwFolder(), "users.dat");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(userDat);
            for (int i = 0; i < usernames.size(); i++) {
                fileOutputStream.write((usernames.get(i) + "\n").getBytes());
            }
            fileOutputStream.close();

            userDat.setReadable(true);
            MediaScannerConnection.scanFile(context, new String[] {userDat.toString()}, null, null);
        } catch (FileNotFoundException e) {
            Log.e("FileManager", "Unable to create user.dat to write");
        } catch (IOException e) {
            Log.e("FileManager", "Unable to write user.dat");
        }
    }

    public static void recreateWordsDatFile(List<Word> words, String username, Context context) {
        File wordsDat = new File(getUserFolderString(username), "words.dat");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(wordsDat);
            for (int i = 0; i < words.size(); i++) {
                fileOutputStream.write((words.get(i).getWordName() + " " +
                        words.get(i).getWord_id() + " \n").getBytes());
            }
            fileOutputStream.close();

            wordsDat.setReadable(true);
            MediaScannerConnection.scanFile(context, new String[] {wordsDat.toString()}, null, null);
        } catch (FileNotFoundException e) {
            Log.e("FileManager", "Unable to create words.dat to write");
        } catch (IOException e) {
            Log.e("FileManager", "Unable to write words.dat");
        }
    }

    public static void recreateRepetitionDatFile(List<Recording> recordings, String username, Context context) {
        File labelsDat = new File(getUserFolderString(username), "Calibration/labels.dat");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(labelsDat);
            for (int i = 0; i < recordings.size(); i++) {
                if (recordings.get(i).getFileLocation() != null) {
                    String writeString = String.valueOf(recordings.get(i).getWordId()) + " " +
                            String.valueOf(recordings.get(i).getRepetitionNumber());
                    if (recordings.get(i).isCorrect()) {
                        writeString += " c \n";
                    } else {
                        writeString += " i \n";
                    }
                    fileOutputStream.write(writeString.getBytes());
                }
            }
            fileOutputStream.close();

            labelsDat.setReadable(true);
            MediaScannerConnection.scanFile(context, new String[] {labelsDat.toString()}, null, null);
        } catch (FileNotFoundException e) {
            Log.e("FileManager", "Unable to create labels.dat to write");
        } catch (IOException e) {
            Log.e("FileManager", "Unable to write labels.dat");
        }
    }

    public static String getUserFolderString(String username) {
        return  getAwFolder().toString() + "/" + username;
    }

    private static File getAwFolder() {
        return new File(Environment.getExternalStorageDirectory() + "/Apraxia World Audio");
    }

}
