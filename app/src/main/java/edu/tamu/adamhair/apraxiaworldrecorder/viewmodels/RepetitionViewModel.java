package edu.tamu.adamhair.apraxiaworldrecorder.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.AppDatabase;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;
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

    public void populateRepetitions(final String[] words, User user, Intent intent, Application application) {
        new addAsyncTask(appDatabase, user, intent, application).execute(words);
    }

    private static class addAsyncTask extends AsyncTask<String, Void, Void> {
        private AppDatabase db;
        private User user;
        private Intent intent;
        private Application application;
        private int userId;
        private int numWords = 30;
        private int numReps = 10;

        addAsyncTask(AppDatabase appDatabase, User user, Intent intent, Application application) {
            db = appDatabase;
            this.user = user;
            this.intent = intent;
            this.application = application;
        }

        private int[] randomIndices(int numIndices, int maxIndex) {
            ArrayList<Integer> indices = new ArrayList<>();
            for (int i = 0; i < maxIndex; i++) {
                indices.add(i);
            }
            Collections.shuffle(indices);

            int[] selectedIndices = new int[numIndices];
            for (int i = 0; i < numIndices; i++) {
                selectedIndices[i] = indices.get(i);
            }

            return selectedIndices;
        }

        @Override
        protected Void doInBackground(final String... params) {
            db.userDao().insertAll(this.user);
            User user = db.userDao().findByUsername(this.user.getUsername());
            this.userId = user.getUid();

            Repetition[] repetitions = new Repetition[numWords];

            int[] wordIndices = randomIndices(numWords, params.length);

            for (int i = 0; i < numWords; i++) {
                repetitions[i] = new Repetition(user.getUid(), params[wordIndices[i]], db.wordDao().findIdByWord(params[wordIndices[i]]), 0, 0);
            }
            Log.d("Repetition", "Repetitions ok");

            Recording[] recordings = new Recording[numWords*numReps];
            int idx = 0;
            for (int i = 0; i < numWords; i++) {
                for (int j = 0; j < numReps; j++) {
                    recordings[idx] = new Recording(user.getUid(), null, db.wordDao().findIdByWord(params[wordIndices[i]]),
                            true, j+1);
                    idx++;
                }
            }
            Log.d("Recording", "Recordings ok");


            db.repetitionDao().insertAll(repetitions);
            db.recordingDao().insertAll(recordings);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            intent.putExtra("userId", this.userId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            application.startActivity(intent);
        }
    }}
