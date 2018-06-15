package edu.tamu.adamhair.apraxiaworldrecorder.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.AppDatabase;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Word;

public class WordViewModel extends AndroidViewModel {

    private AppDatabase appDatabase;

    public WordViewModel(Application application) {
        super(application);

        appDatabase = AppDatabase.getAppDatabase(this.getApplication());
    }

    public String getWordById(int wordId) {
        return appDatabase.wordDao().findWordById(wordId);
    }

    public List<Integer> getWordIdsContainingSubstring(String substring) {
        substring = substring + "%";
        return appDatabase.wordDao().findIdsForWordsContainingSubstring(substring);
    }

    public List<Word> getAllWords() { return appDatabase.wordDao().getAllList(); }

}
