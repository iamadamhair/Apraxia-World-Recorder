package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class WordRecorderActivity extends AppCompatActivity {

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

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        int imageId = intent.getIntExtra("imageId",0);

        titleTextView.setText(title.substring(0,1).toUpperCase() + title.substring(1));
        thumbnailImageView.setImageResource(imageId);

        ArrayList<String> repetitionTitles = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            repetitionTitles.add("Rep " + String.valueOf(i));
        }

        RepetitionListAdapter repetitionListAdapter = new RepetitionListAdapter(this, repetitionTitles);
        repetitionListView.setAdapter(repetitionListAdapter);

        correctTextView.setText("Correct: #");
        incorrectTextView.setText("Incorrect: #");
        correctInfoTextView.setText("");
        incorrectInfoTextView.setText("");

        setTitle("Word Recorder");
    }
}
