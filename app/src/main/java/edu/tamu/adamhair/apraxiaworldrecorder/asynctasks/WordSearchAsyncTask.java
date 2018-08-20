package edu.tamu.adamhair.apraxiaworldrecorder.asynctasks;

import android.os.AsyncTask;

import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.WordListAdapter;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.WordViewModel;

public class WordSearchAsyncTask extends AsyncTask<Void, Void, Void> {
    private WordListAdapter wordListAdapter;
    private RepetitionViewModel repetitionViewModel;
    private WordViewModel wordViewModel;
    private String substring;
    private int userId;
    private List<Repetition> repetitionMatches;

    public WordSearchAsyncTask(WordListAdapter wordListAdapter, RepetitionViewModel repetitionViewModel,
                        WordViewModel wordViewModel, String substring, int userId) {
        this.wordListAdapter = wordListAdapter;
        this.repetitionViewModel = repetitionViewModel;
        this.substring = substring;
        this.wordViewModel = wordViewModel;
        this.userId = userId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (this.substring.isEmpty()) {
            // Replace it with anything so that the database doesn't return all the words
            this.substring = ".";
        }
        List<Integer> wordIds = wordViewModel.getWordIdsContainingSubstring(this.substring);
        this.repetitionMatches = repetitionViewModel.getRepetitionListByUserIdAndWordIdsSorted(this.userId, wordIds);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        this.wordListAdapter.addItems(this.repetitionMatches);
    }
}
