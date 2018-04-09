package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class WordSelectionActivity extends AppCompatActivity {

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


        String[] words = getResources().getStringArray(R.array.ndp3_images);
        ArrayList<String> wordArrayList = new ArrayList<String>();
        for (int i = 0; i < words.length; i++) {
            wordArrayList.add(words[i]);
        }

        WordListAdapter wordListAdapter = new WordListAdapter(this, wordArrayList);
//        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, words);
        wordList.setAdapter(wordListAdapter);

        Intent intent = getIntent();
        wordSelectionUsername.setText(intent.getStringExtra("username"));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
    }
}
