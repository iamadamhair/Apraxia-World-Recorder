package edu.tamu.adamhair.apraxiaworldrecorder.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.ProbeWordListAdapter;
import edu.tamu.adamhair.apraxiaworldrecorder.database.AppDatabase;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Probe;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;

public class ProbeViewModel extends AndroidViewModel{

    private AppDatabase appDatabase;

    public ProbeViewModel(Application application) {
        super(application);

        appDatabase = AppDatabase.getAppDatabase(this.getApplication());
    }

    public void addProbes(Probe... probes) {
        new addAsyncTask(appDatabase).execute(probes);
    }

    public void updateProbes(Probe... probes) {
        new updateAsyncTask(appDatabase).execute(probes);
    }

    public void createNewProbe(int userId, ProbeWordListAdapter probeWordListAdapter) {
        new createNewProbeAsyncTask(appDatabase, userId, probeWordListAdapter).execute();
    }

    public LiveData<List<Probe>> getUniqueProbesForUser(int userId) {
        return appDatabase.probeDao().getUniqueProbesForUser(userId);
    }

    public LiveData<List<Integer>> getUniqueProbeNumbers(int userId) {
        return appDatabase.probeDao().getUniqueProbeNumbers(userId);
    }

    public LiveData<List<Probe>> getProbesForNumber(int probeNum) {
        return appDatabase.probeDao().getProbesForNumber(probeNum);
    }

    public static class addAsyncTask extends AsyncTask<Probe, Void, Void> {
        private AppDatabase db;

        addAsyncTask(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(final Probe... probes) {
            db.probeDao().insertAll(probes);
            return null;
        }
    }

    public static class updateAsyncTask extends AsyncTask<Probe, Void, Void> {
        private AppDatabase db;

        updateAsyncTask(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(final Probe... probes) {
            db.probeDao().update(probes);
            return null;
        }
    }

    public static class createNewProbeAsyncTask extends AsyncTask<Void, Void, Void> {
        private AppDatabase db;
        private int userId;
        private ProbeWordListAdapter probeWordListAdapter;
        private int probeNum;
        private List<Probe> probes;

        createNewProbeAsyncTask(AppDatabase db, int userId, ProbeWordListAdapter probeWordListAdapter) {
            this.db = db;
            this.userId = userId;
            this.probeWordListAdapter = probeWordListAdapter;
        }

        @Override
        protected Void doInBackground(final Void... voids) {
            List<Repetition> repetitions = db.repetitionDao().findRepetitionListToExport(userId);
            int prevMaxProbNum = -1;

            Probe[] newProbes = new Probe[repetitions.size()];

            int probeCount = db.probeDao().getProbeCountForUser(userId);
            if (probeCount > 0) {
                prevMaxProbNum = db.probeDao().getMaxProbeNumForUser(userId);
            }

            probeNum = prevMaxProbNum + 1;

            // Generate date string
            Calendar calendar = Calendar.getInstance();
            int[] dateValues = new int[6];
            dateValues[0] = calendar.get(Calendar.YEAR);
            dateValues[1] = calendar.get(Calendar.MONTH) + 1; // Starts at 0
            dateValues[2] = calendar.get(Calendar.DAY_OF_MONTH);
            dateValues[3] = calendar.get(Calendar.HOUR_OF_DAY);
            dateValues[4] = calendar.get(Calendar.MINUTE);
            dateValues[5] = calendar.get(Calendar.SECOND);
            String dateString = Integer.toString(dateValues[2]) + "-" + Integer.toString(dateValues[1]) + "-" +
                    Integer.toString(dateValues[0]) + " " + Integer.toString(dateValues[3]) + "_" + Integer.toString(dateValues[4]) +
                    "_" + Integer.toString(dateValues[5]);

            for (int i = 0; i < newProbes.length; i++) {
                newProbes[i] = new Probe(probeNum, userId, "", repetitions.get(i).getWordId(), repetitions.get(i).getWordName(), false, dateString);
            }

            db.probeDao().insertAll(newProbes);
            probes = Arrays.asList(newProbes);
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            probeWordListAdapter.addItems(probes);
        }
    }
}
