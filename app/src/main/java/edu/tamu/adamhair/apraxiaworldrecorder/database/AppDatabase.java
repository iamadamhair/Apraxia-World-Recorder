package edu.tamu.adamhair.apraxiaworldrecorder.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import edu.tamu.adamhair.apraxiaworldrecorder.R;

@Database(entities = {User.class, Recording.class, Repetition.class, Word.class, Probe.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract RecordingDao recordingDao();
    public abstract RepetitionDao repetitionDao();
    public abstract WordDao wordDao();
    public abstract ProbeDao probeDao();

    public static AppDatabase getAppDatabase(final Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                    "apraxia-world-database").addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);

                            // Populate the database with the word list
                            Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    getAppDatabase(context).wordDao().insertAll(AppDatabase.createWordList(context));
                                }
                            });
                        }
            }).addMigrations(MIGRATION_1_2).build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public static Word[] createWordList(Context context) {
        String[] ndp3Array = context.getResources().getStringArray(R.array.ndp3_images);
        List<Word> wordArray = new ArrayList<>();
        for (int i = 0; i < ndp3Array.length; i++) {
            wordArray.add(new Word(ndp3Array[i]));
        }
        return wordArray.toArray(new Word[wordArray.size()]);
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE 'probe' ('probe_id' INTEGER PRIMARY KEY NOT NULL, " +
                    "'probe_number' INTEGER NOT NULL, " +
                    "'user_id' INTEGER NOT NULL, " +
                    "'file_location' TEXT, " +
                    "'word_id' INTEGER NOT NULL, " +
                    "'word_name' TEXT," +
                    "'is_correct' INTEGER NOT NULL, " +
                    "'probe_date' TEXT)");
        }
    };
}
