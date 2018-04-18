package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;
import edu.tamu.adamhair.apraxiaworldrecorder.database.RepetitionDao;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RecordingViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;

public class WordRecorderActivity extends AppCompatActivity {

    private ArrayList<Recording> recordings;
    private RecordingViewModel recordingViewModel;
    private RepetitionViewModel repetitionViewModel;
    private RepetitionListAdapter repetitionListAdapter;

    private int userId;
    private int wordId;
    private String username;
    private String word;

    /* UI elements */
    TextView titleTextView;
    TextView correctTextView;
    TextView incorrectTextView;
    TextView correctInfoTextView;
    TextView incorrectInfoTextView;
    ListView repetitionListView;
    ImageView thumbnailImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_recorder);

        /* Configure UI */
        titleTextView = (TextView) findViewById(R.id.repetitionTitleTextView);
        correctTextView = (TextView) findViewById(R.id.repetitionCorrectTextView);
        incorrectTextView = (TextView) findViewById(R.id.repetitionIncorrectTextView);
        correctInfoTextView = (TextView) findViewById(R.id.repetitionCorrectInfoTextView);
        incorrectInfoTextView = (TextView) findViewById(R.id.repetitionIncorrectInfoTextView);
        thumbnailImageView = (ImageView) findViewById(R.id.repetitionThumbnailImageView);
        repetitionListView = (ListView) findViewById(R.id.repetitionListView);

        /* Get values from the intent */
        Intent intent = getIntent();
        word = intent.getStringExtra("title");
        username = intent.getStringExtra("username");
        int imageId = intent.getIntExtra("imageId",0);
        userId = intent.getIntExtra("userId", 0);
        wordId = intent.getIntExtra("wordId", 0);

        titleTextView.setText(word.substring(0,1).toUpperCase() + word.substring(1));
        thumbnailImageView.setImageResource(imageId);

        /* Set up listview with empty arraylist that will be filled later by livedata */
        recordings = new ArrayList<>();
        repetitionListAdapter = new RepetitionListAdapter(this, recordings, correctTextView, incorrectTextView);
        repetitionListView.setAdapter(repetitionListAdapter);
        repetitionListAdapter.setUsername(username);
        repetitionListAdapter.setWord(word);

        correctTextView.setText("Correct: #");
        incorrectTextView.setText("Incorrect: #");
        correctInfoTextView.setText("");
        incorrectInfoTextView.setText("");

        setTitle("Word Recorder");

        Log.d("RecorderActivity", String.valueOf(userId) + ", " + String.valueOf(wordId));

        /* Set up LiveData */
        recordingViewModel = ViewModelProviders.of(this).get(RecordingViewModel.class);
        repetitionListAdapter.setRecordingViewModel(recordingViewModel);
        recordingViewModel.getRecordingsOfUserAndWord(userId, wordId).observe(WordRecorderActivity.this, new Observer<List<Recording>>() {
            @Override
            public void onChanged(@Nullable List<Recording> recordings) {
                repetitionListAdapter.addItems(new ArrayList(recordings));
            }
        });
        repetitionViewModel = ViewModelProviders.of(this).get(RepetitionViewModel.class);
        repetitionListAdapter.setRepetitionViewModel(repetitionViewModel);

        /* Create word folder, if necessary */
        if (!FileManager.wordFolderExists(username, word)) {
            FileManager.createWordFolder(username, word);
            Log.d("Word Recorder", "Making word folder");
        }
    }

    @Override
    public void onStop() {
        repetitionListAdapter.releaseMediaRecorders();
        super.onStop();
    }
}
