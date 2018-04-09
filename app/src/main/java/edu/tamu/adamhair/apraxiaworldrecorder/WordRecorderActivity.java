package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class WordRecorderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_recorder);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        Log.d("Title", title);

        setTitle("Word Recorder");
    }
}
