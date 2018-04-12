package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;

public class WordSelectionActivity extends AppCompatActivity {

    RepetitionViewModel repetitionViewModel;
    int userId;
    String username;

    /* UI elements */
    ListView wordList;
    TextView wordSelectionUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_selection);

        /* Configure UI elements */
        wordList = findViewById(R.id.wordsListView);
        wordSelectionUsername = findViewById(R.id.wordSelectionUsernameTextView);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        wordSelectionUsername.setText(username);
        userId = intent.getIntExtra("userId", 0);

        /* Set up ListView with empty ArrayList until LiveData fills it in */
        ArrayList<Repetition> repetitions = new ArrayList<>();
        final WordListAdapter wordListAdapter = new WordListAdapter(getApplication(), repetitions);
        wordList.setAdapter(wordListAdapter);
        wordListAdapter.setUsername(username);

        /* Set up LiveData */
        repetitionViewModel =  ViewModelProviders.of(this).get(RepetitionViewModel.class);

        repetitionViewModel.getRepetitionsByUserIdSorted(userId).observe(WordSelectionActivity.this, new Observer<List<Repetition>>() {
            @Override
            public void onChanged(@Nullable List<Repetition> repetitions) {
                wordListAdapter.addItems(repetitions);
            }
        });

        /* Set up user folder, if necessary */
        if (!FileManager.userFolderExists(username)) {
            FileManager.createUserFolder(username);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
    }
}
