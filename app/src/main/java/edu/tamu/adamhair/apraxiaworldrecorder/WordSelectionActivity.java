package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;

public class WordSelectionActivity extends AppCompatActivity {

    RepetitionViewModel repetitionViewModel;
    int userId;
    String username;
    int wordsCompleted;

    /* Firebase variables */
    StorageReference storageReference;

    /* UI elements */
    ListView wordList;
    TextView wordSelectionUsername;
    TextView correctCountTextView;
    ImageButton cloudUploadImagebutton;
    ImageButton gameExportImageButton;

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
            }
        });

        /* Set up user folder, if necessary */
        if (!FileManager.userFolderExists(username)) {
            FileManager.createUserFolder(username);
        }

        /* Set up onClickListeners */
        gameExportImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wordsCompleted < 10) {
                    Toast.makeText(WordSelectionActivity.this, "You need to record " + String.valueOf(10-wordsCompleted) + " more word sets", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cloudUploadImagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wordsCompleted < 10) {
                    Toast.makeText(WordSelectionActivity.this, "You need to record " + String.valueOf(10-wordsCompleted) + " more word sets", Toast.LENGTH_SHORT).show();
                } else {
                    String params[] = new String[2];
                    params[0] = FileManager.getUserFolderString(username);
                    params[1] = Environment.getExternalStorageDirectory().toString() + "/" + username + ".zip";
                    new zipAsyncTask(WordSelectionActivity.this).doInBackground(params);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
    }

    private static class zipAsyncTask extends AsyncTask<String, Void, Void> {
        /*
        Zip function taken from SO answer:
        https://stackoverflow.com/questions/6683600/zip-compress-a-folder-full-of-files-on-android
         */
        final int BUFFER = 2048;
        private Context mContext;

        zipAsyncTask(Context context) {
            mContext = context;
        }


        @Override
        protected Void doInBackground(final String... params) {
            // Param 0 is source, param 1 is destination
            zipFileAtPath(params[0], params[1]);
            return null;
        }

        private boolean zipFileAtPath(String sourcePath, String destinationPath) {
            File sourceFile = new File(sourcePath);

            try {
                BufferedInputStream origin = null;
                FileOutputStream destination = new FileOutputStream(destinationPath);
                ZipOutputStream outputZip = new ZipOutputStream(new BufferedOutputStream(destination));

                if (sourceFile.isDirectory()) {
                    zipSubFolder(outputZip, sourceFile, sourceFile.getParent().length()+1);
                } else {
                    byte data[] = new byte[BUFFER];
                    FileInputStream fi = new FileInputStream(sourcePath);
                    origin = new BufferedInputStream(fi, BUFFER);

                    ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath.substring(sourcePath.lastIndexOf("/"))));
                    outputZip.putNextEntry(entry);

                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1 ) {
                        outputZip.write(data, 0, count);
                    }
                }
                outputZip.close();
                MediaScannerConnection.scanFile(mContext, new String[] {destinationPath}, null, null);

            } catch (Exception e) {
                e.printStackTrace();;
                return false;
            }
            return true;
        }

        private void zipSubFolder(ZipOutputStream out, File folder, int basePathLength) throws IOException {

            final int BUFFER = 2048;

            File[] fileList = folder.listFiles();
            BufferedInputStream origin = null;
            for (File file : fileList) {
                if (file.isDirectory()) {
                    zipSubFolder(out, file, basePathLength);
                } else {
                    byte data[] = new byte[BUFFER];
                    String unmodifiedFilePath = file.getPath();
                    String relativePath = unmodifiedFilePath
                            .substring(basePathLength);
                    FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(relativePath);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
        }

        private String getLastPathComponent(String filePath) {
            String[] segments = filePath.split("/");
            if (segments.length == 0)
                return "";
            String lastPathComponent = segments[segments.length - 1];
            return lastPathComponent;
        }
    }
}
