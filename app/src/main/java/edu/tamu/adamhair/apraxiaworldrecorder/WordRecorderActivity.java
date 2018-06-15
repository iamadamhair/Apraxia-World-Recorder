package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;
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
    ListView repetitionListView;
    ImageView thumbnailImageView;
    Button mfccButton;
    FrameLayout overlay;
    FrameLayout finishMessage;
    Button finishMessageButton;
    TextView effectSizeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_recorder);

        /* Configure UI */
        titleTextView = (TextView) findViewById(R.id.repetitionTitleTextView);
        correctTextView = (TextView) findViewById(R.id.repetitionCorrectTextView);
        incorrectTextView = (TextView) findViewById(R.id.repetitionIncorrectTextView);
        thumbnailImageView = (ImageView) findViewById(R.id.repetitionThumbnailImageView);
        repetitionListView = (ListView) findViewById(R.id.repetitionListView);
        mfccButton = findViewById(R.id.mfccButton);
        overlay = findViewById(R.id.progressBarHolder);
        finishMessage = findViewById(R.id.effectSizeMessage);
        finishMessageButton = findViewById(R.id.effectSizeButton);
        effectSizeTextView = findViewById(R.id.effectSizeTextView);

        mfccButton.setEnabled(false);

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
        repetitionListAdapter = new RepetitionListAdapter(this, recordings, correctTextView, incorrectTextView, mfccButton);
        repetitionListView.setAdapter(repetitionListAdapter);
        repetitionListAdapter.setUsername(username);
        repetitionListAdapter.setWord(word);

        correctTextView.setText("Correct: #");
        incorrectTextView.setText("Incorrect: #");

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

        /* Create onclick listener */
        mfccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new mfccTask(overlay, finishMessage, effectSizeTextView, repetitionListAdapter).execute();
            }
        });
        finishMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
                outAnimation.setDuration(200);
                finishMessage.setAnimation(outAnimation);
                finishMessage.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStop() {
        repetitionListAdapter.releaseMediaPlayers();
        super.onStop();
    }

    private static class mfccTask extends AsyncTask<Void, Void, Void> {
        private FrameLayout overlay;
        private FrameLayout finishMessage;
        private TextView messageTextView;
        private AlphaAnimation inAnimation;
        private RepetitionListAdapter repetitionListAdapter;
        private double effectSize;

         mfccTask(FrameLayout processingFrameLayout, FrameLayout finishMessage,
                  TextView effectSizeMessage, RepetitionListAdapter repetitionListAdapter) {
            this.overlay = processingFrameLayout;
            this.finishMessage = finishMessage;
            this.messageTextView = effectSizeMessage;
            this.repetitionListAdapter = repetitionListAdapter;
            this.inAnimation = new AlphaAnimation(0f, 1f);
            this.inAnimation.setDuration(200);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.overlay.setAnimation(inAnimation);
            this.overlay.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            this.overlay.setVisibility(View.GONE);
            this.finishMessage.setVisibility(View.VISIBLE);
            this.messageTextView.setText("Effect size is " + new DecimalFormat("#0.000").format(effectSize));
        }

        @Override
        protected Void doInBackground(Void... params) {
            effectSize = repetitionListAdapter.getEffectSize();
            return null;
        }
    }
}
