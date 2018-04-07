package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class WordSelectionActivity extends AppCompatActivity {

    ListView wordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_selection);
        wordList = findViewById(R.id.wordsListView);

        String[] words = {"Elephant", "Helicopter", "Arm", "Bear"};
        ArrayList<String> wordArrayList = new ArrayList<String>();
        for (int i = 0; i < words.length; i++) {
            wordArrayList.add(words[i]);
        }

        WordListAdapter wordListAdapter = new WordListAdapter(this, wordArrayList);
//        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, words);
        wordList.setAdapter(wordListAdapter);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
    }
}
