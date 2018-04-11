package edu.tamu.adamhair.apraxiaworldrecorder.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.AppDatabase;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
import edu.tamu.adamhair.apraxiaworldrecorder.database.User;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Word;

public class RepetitionViewModel extends AndroidViewModel {

    private AppDatabase appDatabase;
    private LiveData<List<Repetition>> repetitions;

    public RepetitionViewModel(Application application){
        super(application);

        appDatabase = AppDatabase.getAppDatabase(this.getApplication());
        repetitions = appDatabase.repetitionDao().getAll();
    }

    public LiveData<List<Repetition>> getRepetitions() {
        return repetitions;
    }

    public LiveData<List<Repetition>> getRepetitionsByUserId(int userId) {
        return appDatabase.repetitionDao().findByUserId(userId);
    }
//
//    public void addRepetitions(final Repetition[] repetitions) {
//        new addAsyncTask(appDatabase).execute(repetitions);
//    }

    public void populateRepetitions(final String[] words, User user, Intent intent, Application application) {
        new addAsyncTask(appDatabase, user, intent, application).execute(words);
    }


    private static class addAsyncTask extends AsyncTask<String, Void, Void> {
        private AppDatabase db;
        private User user;
        private Intent intent;
        private Application application;
        private int userId;

        addAsyncTask(AppDatabase appDatabase, User user, Intent intent, Application application) {
            db = appDatabase;
            this.user = user;
            this.intent = intent;
            this.application = application;
        }

        @Override
        protected Void doInBackground(final String... params) {
            db.userDao().insertAll(this.user);
            User user = db.userDao().findByUsername(this.user.getUsername());
            this.userId = user.getUid();

            Repetition[] repetitions = new Repetition[params.length];

            for (int i = 0; i < params.length; i++) {
                repetitions[i] = new Repetition(user.getUid(), params[i], db.wordDao().findIdByWord(params[i]), 0, 0);
                Log.d("Repetition", params[i]);
            }

            db.repetitionDao().insertAll(repetitions);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            intent.putExtra("userId", this.userId);
            application.startActivity(intent);
        }
    }}
