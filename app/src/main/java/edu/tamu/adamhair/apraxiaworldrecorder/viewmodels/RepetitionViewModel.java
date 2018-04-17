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

    public RepetitionViewModel(Application application ){
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

    public LiveData<List<Repetition>> getRepetitionsByUserIdSorted(int userId) {
        return appDatabase.repetitionDao().findByUserIdSorted(userId);
    }

    public void populateRepetitions(final String[] words, User user, Intent intent, Application application) {
        new addAsyncTask(appDatabase, user, intent, application).execute(words);
    }

    public List<Repetition> getRepetitionsListByUser(int userId) {return appDatabase.repetitionDao().getAllOfAUserList(userId);}

    public void updateWordLabelCounts(int wordId, int userId, int correct, int incorrect) {
        Integer[] params = new Integer[4];
        params[0] = userId;
        params[1] = wordId;
        params[2] = correct;
        params[3] = incorrect;
        new updateCountsAsyncTask(appDatabase).execute(params);
    }

    private static class updateCountsAsyncTask extends AsyncTask<Integer, Void, Void> {
        AppDatabase appDatabase;

        updateCountsAsyncTask(AppDatabase db) {
            appDatabase = db;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            Repetition repetition = appDatabase.repetitionDao().findRepetitionByUserIdAndWordId(integers[0], integers[1]);
            repetition.setNumCorrect(integers[2]);
            repetition.setNumIncorrect(integers[3]);
            appDatabase.repetitionDao().update(repetition);

            return null;
        }
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

            Recording[] recordings = new Recording[numWords*numReps];
            int idx = 0;
            for (int i = 0; i < numWords; i++) {
                for (int j = 0; j < numReps; j++) {
                    recordings[idx] = new Recording(user.getUid(), null, db.wordDao().findIdByWord(params[wordIndices[i]]),
                            false, j+1);
                    Log.d("Repetition", String.valueOf(j+1));
                    idx++;
                }
            }

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
    }
}
