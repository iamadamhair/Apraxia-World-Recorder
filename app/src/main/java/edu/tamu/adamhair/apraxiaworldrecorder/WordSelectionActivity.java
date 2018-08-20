package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.tamu.adamhair.apraxiaworldrecorder.asynctasks.ExportAudioAsyncTask;
import edu.tamu.adamhair.apraxiaworldrecorder.asynctasks.WordSearchAsyncTask;
import edu.tamu.adamhair.apraxiaworldrecorder.asynctasks.ZipAsyncTask;
import edu.tamu.adamhair.apraxiaworldrecorder.audio.AudioProcessor;
import edu.tamu.adamhair.apraxiaworldrecorder.audio.LibrosaStyleDTW;
import edu.tamu.adamhair.apraxiaworldrecorder.audio.MFCC;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
import edu.tamu.adamhair.apraxiaworldrecorder.database.User;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Word;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RecordingViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.UserViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.WordViewModel;

public class WordSelectionActivity extends AppCompatActivity {

    RepetitionViewModel repetitionViewModel;
    RecordingViewModel recordingViewModel;
    WordViewModel wordViewModel;
    UserViewModel userViewModel;
    int userId;
    String username;
    int wordsCompleted;
    List<Repetition> repetitionList;

    /* Firebase variables */
    StorageReference storageReference;

    /* UI elements */
    ListView wordList;
    TextView wordSelectionUsername;
    TextView correctCountTextView;
    ImageButton cloudUploadImagebutton;
    ImageButton gameExportImageButton;
    FrameLayout uploadFrameLayout;
    FrameLayout exportFrameLayout;
    FrameLayout confirmationFrameLayout;
    EditText wordSearchEditText;
    ImageButton menuImageButton;

    WordListAdapter wordListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_selection);

        wordsCompleted = 0;
        storageReference = FirebaseStorage.getInstance().getReference();

        /* Configure UI elements */
        wordList = findViewById(R.id.wordsListView);
        wordSelectionUsername = findViewById(R.id.wordSelectionUsernameTextView);
        correctCountTextView = findViewById(R.id.wordsCompletedTextView);
        cloudUploadImagebutton = findViewById(R.id.cloudUploadImageButton);
        gameExportImageButton = findViewById(R.id.gameExportImageButton);
        uploadFrameLayout = findViewById(R.id.uploadFrameLayout);
        exportFrameLayout = findViewById(R.id.exportFrameLayout);
        confirmationFrameLayout = findViewById(R.id.exportConfirmFrameLayout);
        wordSearchEditText = findViewById(R.id.wordSearchEditText);
        menuImageButton = findViewById(R.id.menuImageButton);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        wordSelectionUsername.setText(username);
        userId = intent.getIntExtra("userId", 0);

        /* Set up ListView with empty ArrayList until LiveData fills it in */
        ArrayList<Repetition> repetitions = new ArrayList<>();
        wordListAdapter = new WordListAdapter(getApplication(), repetitions);
        wordList.setAdapter(wordListAdapter);
        wordListAdapter.setUsername(username);

        /* Set up LiveData */
        repetitionViewModel =  ViewModelProviders.of(this).get(RepetitionViewModel.class);
        recordingViewModel = ViewModelProviders.of(this).get(RecordingViewModel.class);
        wordViewModel = ViewModelProviders.of(this).get(WordViewModel.class);
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        // getRepetitionsByUserIdSorted
        repetitionViewModel.getRepetitionsMarkedForExport(userId).observe(WordSelectionActivity.this, new Observer<List<Repetition>>() {
            @Override
            public void onChanged(@Nullable List<Repetition> repetitions) {
//                wordListAdapter.addItems(repetitions);

                // Get number of completed recording sets
                int count = 0;
                for (int i = 0; i < repetitions.size(); i++) {
                    if (repetitions.get(i).getNumCorrect() >= 5 &&
                            repetitions.get(i).getNumIncorrect() >= 5) {
                        count++;
                    }
                }
                correctCountTextView.setText("Completed words: " + String.valueOf(count));
                wordsCompleted = count;
                repetitionList = repetitions;
            }
        });

        /* Set up user folder, if necessary */
        if (!FileManager.userFolderExists(username)) {
            FileManager.createUserFolder(username);
        }

        /* Set up onClickListeners */
        wordSearchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    hideKeyboard(view);
                }
            }
        });

        wordList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard(wordSearchEditText);
                return false;
            }
        });

        wordSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String substring = editable.toString();
                new WordSearchAsyncTask(wordListAdapter, repetitionViewModel, wordViewModel, substring, userId).execute();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new WordSearchAsyncTask(wordListAdapter, repetitionViewModel, wordViewModel,
                wordSearchEditText.getText().toString(), userId).execute();

    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        popup.inflate(R.menu.word_action_menu);

        // Add onclick listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.showSelected) {

                    repetitionViewModel.getRepetitionsMarkedForExport(userId).observe(WordSelectionActivity.this, new Observer<List<Repetition>>() {
                        @Override
                        public void onChanged(@Nullable List<Repetition> repetitions) {
                            wordListAdapter.addItems(repetitions);
                        }
                    });

                } else if (menuItem.getItemId() == R.id.probe) {

                    if (wordsCompleted < 10) {
                        Toast.makeText(WordSelectionActivity.this, "You need to record " + String.valueOf(10 - wordsCompleted) + " more word sets before probing", Toast.LENGTH_SHORT).show();
                    } else {
                        launchProbeSelectionActivity();
                    }

                } else if (menuItem.getItemId() == R.id.uploadToCloud) {

                    // See if enough words are completed before uploading the audio
                    if (wordsCompleted < 10) {
                        Toast.makeText(WordSelectionActivity.this, "You need to record " + String.valueOf(10-wordsCompleted) + " more word sets", Toast.LENGTH_SHORT).show();
                    } else {
                        String params[] = new String[2];
                        params[0] = FileManager.getUserFolderString(username);
                        params[1] = Environment.getExternalStorageDirectory().toString() + "/" + username + ".zip";
                        new ZipAsyncTask(WordSelectionActivity.this, storageReference,
                                recordingViewModel, wordViewModel, userId, username, uploadFrameLayout,
                                confirmationFrameLayout, false).execute(params);
                    }

                } else if (menuItem.getItemId() == R.id.exportToGame) {

                    // See if enough words are completed before exporting the audio
                    if (wordsCompleted < 10) {
                        Toast.makeText(WordSelectionActivity.this, "You need to select " + String.valueOf(10-wordsCompleted) + " more word sets", Toast.LENGTH_SHORT).show();
                    } else {
                        userViewModel.writeUsernamesMarkedForExport(WordSelectionActivity.this);
                        new ExportAudioAsyncTask(WordSelectionActivity.this, exportFrameLayout, confirmationFrameLayout, userId,
                                username, repetitionList, recordingViewModel, wordViewModel).execute();
                    }

                }
                return true;
            }
        });

        popup.show();
    }

    private void launchProbeSelectionActivity() {
        Intent intent = new Intent(this, ProbeSelectionActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

}
