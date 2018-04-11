package edu.tamu.adamhair.apraxiaworldrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;

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
                Calibration
                    wordsAndLabels.dat
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
        File userFolder = new File(getAwFolder() + "/" + username);
        return userFolder.isDirectory();
    }

    public static void checkAndRequestPermissions(Activity activity) {
        int request_response = 0;
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    request_response);
        }
    }

    private static File getAwFolder() {
        return new File(Environment.getRootDirectory() + "/Apraxia World Audio");
    }

}
