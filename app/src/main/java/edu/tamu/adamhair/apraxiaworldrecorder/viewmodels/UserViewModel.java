package edu.tamu.adamhair.apraxiaworldrecorder.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.FileManager;
import edu.tamu.adamhair.apraxiaworldrecorder.database.AppDatabase;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
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

    public LiveData<List<User>> getAllUsersSorted() { return appDatabase.userDao().getAllSorted(); }

    public LiveData<List<String>> getUsernames() {
        return usernames;
    }

    public LiveData<List<String>> getUsernamesSorted() { return appDatabase.userDao().getAllUsernamesSorted(); }

    public void writeUsernamesMarkedForExport(Context context) {
        new exportUsersAsyncTask(context, appDatabase).execute();
    }

    private static class exportUsersAsyncTask extends AsyncTask<Void, Void, Void> {
        Context context;
        AppDatabase appDatabase;

        public exportUsersAsyncTask(Context context, AppDatabase appDatabase) {
            this.context = context;
            this.appDatabase = appDatabase;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            writeUsersWithEnoughWordsToExport();
            return null;
        }

        private void writeUsersWithEnoughWordsToExport() {
            List<User> users= appDatabase.userDao().getAllList();
            List<String> usernamesToExport = new ArrayList<>();
            for (int i = 0; i < users.size(); i++) {
                List<Repetition> repetitions = appDatabase.repetitionDao().findRepetitionListToExport(users.get(i).getUid());

                int count = 0;
                for (int j = 0; j < repetitions.size(); j++) {
                    if (repetitions.get(j).getNumCorrect() >= 5 && repetitions.get(j).getNumIncorrect() >=5) {
                        count++;
                    }
                }
                if (count >= 10) {
                    usernamesToExport.add(users.get(i).getUsername());
                }
            }
            FileManager.recreateUserDatFile(usernamesToExport, context);
        }
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
