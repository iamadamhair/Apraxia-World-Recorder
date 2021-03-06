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
import java.util.ArrayList;
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
                        distances.dat
                        rep1.wav
                        rep1_mfcc.dat
                        ...
                Game Recordings
                    wordName
                        timestamp.wav
                        ...
                Probe
                    Date
                        labels.txt
                        word_1.wav
                        ...
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

    public static boolean checkPermissions(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean wordFolderExists(String username, String word) {
        File wordFolder = new File(getUserFolderString(username), "Calibration/" + word);
        return wordFolder.isDirectory();
    }

    public static void createWordFolder(String username, String word) {
        File wordFolder = new File(getUserFolderString(username), "Calibration/" +  word);
        wordFolder.mkdir();
    }

    public static boolean probeDateFolderExists(String username, String dateString) {
        return new File(getProbeFolder(username), dateString).isDirectory();
    }

    public static File getProbeDateFolder(String username, String dateString) {
        return new File(getProbeFolder(username), dateString);
    }

    public static void createProbeDateFolder(String username, String dateString) {
        getProbeDateFolder(username, dateString).mkdir();
    }

    public static void createProbeFolder(String username) {
        getProbeFolder(username).mkdir();
    }

    public static boolean probeFolderExists(String username) {
        return getProbeFolder(username).isDirectory();
    }

    public static File getProbeFolder(String username) {
        return new File(getUserFolderString(username), "Probe");
    }

    public static void clearCalibrationAudio(String username) {
        /*
        Not sure why I wanted this function.
        DONT USE UNTIL I REMEMBER, IT DELETES THE WHOLE CALIBRATION FOLDER
         */

        List<File> wordFolders = new ArrayList<>();
        File calibration = new File(getUserFolderString(username), "Calibration");
        File[] calibrationFiles = calibration.listFiles();

        // Save word folder names
        for (int i = 0; i < calibrationFiles.length; i++) {
            if (calibrationFiles[i].isDirectory())
                wordFolders.add(calibrationFiles[i]);
        }
        //deleteRecursively(calibration);

        // Recreate word folders
        for (int i = 0; i < wordFolders.size(); i++) {
            wordFolders.get(i).mkdir();
        }
    }

    private static void deleteRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursively(child);
        fileOrDirectory.delete();
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

    public static void recreateDistanceDatFile(double[] correctDistances, double[] incorrectDistances, String username, String word, Context context) {
        File distanceDat = new File(getUserFolderString(username), "Calibration/" + word + "/distance.dat");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(distanceDat);

            for (int i = 0; i < correctDistances.length; i++) {
                fileOutputStream.write(("c " + String.valueOf(correctDistances[i]) + " \n").getBytes());
            }
            for (int i = 0; i < incorrectDistances.length; i++) {
                fileOutputStream.write(("i " + String.valueOf(incorrectDistances[i]) + " \n").getBytes());
            }
            fileOutputStream.close();

            distanceDat.setReadable(true);
            MediaScannerConnection.scanFile(context, new String[] {distanceDat.toString()}, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("FileManager", "Unable to write/create distance.dat");
        }
    }

    public static void recreateMfccFile(double[][] mfcc, String username, String word, int rep, Context context) {
        File mfccDat = new File(getUserFolderString(username), "Calibration/" + word + "/" +
                String.valueOf(rep) + "_mfcc.dat");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mfccDat);

            fileOutputStream.write((String.valueOf(mfcc.length) + " " + String.valueOf(mfcc[0].length) + " \n").getBytes());

            for (int i = 0; i < mfcc.length; i++) {
                for (int j = 0; j < mfcc[i].length; j++) {
                    fileOutputStream.write((String.valueOf(mfcc[i][j]) + " \n").getBytes());
                }
            }
            fileOutputStream.close();

            mfccDat.setReadable(true);
            MediaScannerConnection.scanFile(context, new String[] {mfccDat.toString()}, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("FileManager", "Unable to write mfcc dat file");
        }
    }

    public static String getUserFolderString(String username) {
        return  getAwFolder().toString() + "/" + username;
    }

    private static File getAwFolder() {
        return new File(Environment.getExternalStorageDirectory() + "/Apraxia World Audio");
    }

}
