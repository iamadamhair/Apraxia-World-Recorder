package edu.tamu.adamhair.apraxiaworldrecorder.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.AppDatabase;
import edu.tamu.adamhair.apraxiaworldrecorder.database.User;

public class UserViewModel extends AndroidViewModel {

    private final LiveData<List<User>> allUsers;
    private final LiveData<List<String>> usernames;
    private AppDatabase appDatabase;

    public UserViewModel(Application application) {
        super(application);

        appDatabase = AppDatabase.getAppDatabase(this.getApplication());
        allUsers = appDatabase.userDao().getAll();
        usernames = appDatabase.userDao().getAllUsernames();
    }

    public void addUser(final User user) {
        new addAsyncTask(appDatabase).execute(user);
    }

    public LiveData<List<User>> getAllUsers() {return allUsers; }

    public LiveData<List<String>> getUsernames() {
        return usernames;
    }

    private static class addAsyncTask extends AsyncTask<User, Void, Void> {
        private AppDatabase db;

        addAsyncTask(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final User... params) {
            db.userDao().insertAll(params[0]);
            return null;
        }
    }
}
