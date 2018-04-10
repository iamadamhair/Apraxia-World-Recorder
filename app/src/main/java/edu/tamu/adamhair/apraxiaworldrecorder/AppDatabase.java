package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {User.class, Recording.class, Repetition.class, Word.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract RecordingDao recordingDao();
    public abstract RepetitionDao repetitionDao();
    public abstract WordDao wordDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                    "apraxia-world-database").allowMainThreadQueries().build();
        }
        Log.d("Database", "Database instance returned");
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
