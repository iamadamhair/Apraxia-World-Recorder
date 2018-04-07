package edu.tamu.adamhair.apraxiaworldrecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WordRecorderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_recorder);

        setTitle("Word Recorder");
    }
}
