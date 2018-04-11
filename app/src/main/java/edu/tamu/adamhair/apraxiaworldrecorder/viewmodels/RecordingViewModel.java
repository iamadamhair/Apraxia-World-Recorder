package edu.tamu.adamhair.apraxiaworldrecorder.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.AppDatabase;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;

public class RecordingViewModel extends AndroidViewModel{
    private AppDatabase appDatabase;
    private LiveData<List<Recording>> recordings;
    private int userId;
    private int wordId;

    public RecordingViewModel(Application application) {
        super(application);

        appDatabase = AppDatabase.getAppDatabase(this.getApplication());
        recordings = appDatabase.recordingDao().getAll();
    }

    public void addRecordings(Recording... recordings) {
        new addAsyncTask(appDatabase).execute(recordings);
    }

    public LiveData<List<Recording>> getRecordings() {
        return recordings;
    }

    public LiveData<List<Recording>> getRecordingsOfUserAndWord(int userId, int wordId) {
        return appDatabase.recordingDao().findByUserIdAndWordId(userId, wordId);
    }

//    public LiveData<List<Recording>> getRecordingsOrPopulate(int userId, int wordId) {
//        return
//    }

    public static class addAsyncTask extends AsyncTask<Recording, Void, Void> {
        private AppDatabase db;

        addAsyncTask(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(final Recording... recordings) {
            db.recordingDao().insertAll(recordings);
            return null;
        }
    }

    public static class populateAsyncTask extends AsyncTask<Recording, Void, Void> {
        private AppDatabase db;

        populateAsyncTask(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(final Recording... recordings) {
            db.recordingDao().insertAll(recordings);
            return null;
        }


    }
}
