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
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;

public class WordSelectionActivity extends AppCompatActivity {

    RepetitionViewModel repetitionViewModel;
    int userId;

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

        ArrayList<Repetition> repetitions = new ArrayList<>();
//        String[] words = getResources().getStringArray(R.array.ndp3_images);
//        ArrayList<String> wordArrayList = new ArrayList<String>();
//        for (int i = 0; i < words.length; i++) {
//            wordArrayList.add(words[i]);
//        }

        final WordListAdapter wordListAdapter = new WordListAdapter(getApplication(), repetitions);
//        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, words);
        wordList.setAdapter(wordListAdapter);

        Intent intent = getIntent();
        wordSelectionUsername.setText(intent.getStringExtra("username"));
        userId = intent.getIntExtra("userId", 0);

        /* Set up LiveData */
        repetitionViewModel =  ViewModelProviders.of(this).get(RepetitionViewModel.class);

        repetitionViewModel.getRepetitionsByUserId(userId).observe(WordSelectionActivity.this, new Observer<List<Repetition>>() {
            @Override
            public void onChanged(@Nullable List<Repetition> repetitions) {
                if (repetitions.size() > 0) {
                    wordListAdapter.addItems(repetitions);
                } else {
                    Log.e("WordSelectionActivity", "Repetition list empty");
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
    }
}
