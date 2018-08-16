package edu.tamu.adamhair.apraxiaworldrecorder.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.AppDatabase;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Probe;

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
        new updateAsyncClass(appDatabase).execute(probes);
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

    public static class updateAsyncClass extends AsyncTask<Probe, Void, Void> {
        private AppDatabase db;

        updateAsyncClass(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(final Probe... probes) {
            db.probeDao().update(probes);
            return null;
        }
    }


}
