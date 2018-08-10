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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

import edu.tamu.adamhair.apraxiaworldrecorder.audio.AudioProcessor;
import edu.tamu.adamhair.apraxiaworldrecorder.audio.LibrosaStyleDTW;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Word;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RecordingViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.WordViewModel;

public class WordSelectionActivity extends AppCompatActivity {

    RepetitionViewModel repetitionViewModel;
    RecordingViewModel recordingViewModel;
    WordViewModel wordViewModel;
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
        gameExportImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wordsCompleted < 10) {
                    Toast.makeText(WordSelectionActivity.this, "You need to select " + String.valueOf(10-wordsCompleted) + " more word sets", Toast.LENGTH_SHORT).show();
                } else {
                    new exportAudioAsyncTask(WordSelectionActivity.this, exportFrameLayout, confirmationFrameLayout, userId,
                            username, repetitionList, recordingViewModel, wordViewModel).execute();
                }
            }
        });
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

        cloudUploadImagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wordsCompleted < 10) {
                    Toast.makeText(WordSelectionActivity.this, "You need to record " + String.valueOf(10-wordsCompleted) + " more word sets", Toast.LENGTH_SHORT).show();
                } else {
                    String params[] = new String[2];
                    params[0] = FileManager.getUserFolderString(username);
                    params[1] = Environment.getExternalStorageDirectory().toString() + "/" + username + ".zip";
                    new zipAsyncTask(WordSelectionActivity.this, storageReference,
                            recordingViewModel, wordViewModel, userId, username, uploadFrameLayout, confirmationFrameLayout).execute(params);
                }
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
                new wordSearchAsyncTask(wordListAdapter, repetitionViewModel, wordViewModel, substring, userId).execute();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new wordSearchAsyncTask(wordListAdapter, repetitionViewModel, wordViewModel,
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

    private static class wordSearchAsyncTask extends AsyncTask<Void, Void, Void> {
        private WordListAdapter wordListAdapter;
        private RepetitionViewModel repetitionViewModel;
        private WordViewModel wordViewModel;
        private String substring;
        private int userId;
        private List<Repetition> repetitionMatches;

        wordSearchAsyncTask(WordListAdapter wordListAdapter, RepetitionViewModel repetitionViewModel,
                            WordViewModel wordViewModel, String substring, int userId) {
            this.wordListAdapter = wordListAdapter;
            this.repetitionViewModel = repetitionViewModel;
            this.substring = substring;
            this.wordViewModel = wordViewModel;
            this.userId = userId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (this.substring.isEmpty()) {
                // Replace it with anything so that the database doesn't return all the words
                this.substring = ".";
            }
            List<Integer> wordIds = wordViewModel.getWordIdsContainingSubstring(this.substring);
            Log.i("Word Ids", wordIds.toString());
            this.repetitionMatches = repetitionViewModel.getRepetitionListByUserIdAndWordIdsSorted(this.userId, wordIds);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            this.wordListAdapter.addItems(this.repetitionMatches);
        }
    }

    private static class exportAudioAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private FrameLayout exportOverlay;
        private FrameLayout confirmationOverlay;
        private int userId;
        private String username;
        private List<Repetition> repetitionList;
        private RecordingViewModel recordingViewModel;
        private WordViewModel wordViewModel;

        exportAudioAsyncTask(Context context, FrameLayout exportOverlay, FrameLayout confirmationOverlay, int userId, String username,
                             List<Repetition> repetitionList, RecordingViewModel recordingViewModel, WordViewModel wordViewModel) {
            this.context = context;
            this.exportOverlay = exportOverlay;
            this.confirmationOverlay = confirmationOverlay;
            this.userId = userId;
            this.username = username;
            this.repetitionList = repetitionList;
            this.recordingViewModel = recordingViewModel;
            this.wordViewModel = wordViewModel;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            this.exportOverlay.setAnimation(inAnimation);
            this.exportOverlay.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            int fs = 16000;
            int frameLength = 512;
            int frameOverlap = 128;
            int padSize = frameLength / 2;

            List<Recording> allRecordings = new ArrayList<>();

            for (int i = 0; i < repetitionList.size(); i++) {
                // Make sure this is a complete word set
                if (repetitionList.get(i).getNumCorrect() > 4 && repetitionList.get(i).getNumIncorrect() > 4) {
                    int wordId = repetitionList.get(i).getWordId();
                    List<Recording> recordings = recordingViewModel.getRecordingArrayListOfUserAndWord(userId, wordId);
                    allRecordings.addAll(recordings);

                    /* Get all MFCCS */
                    List<float[]> audioData = getAudioData(recordings, frameLength, padSize);

                    AudioProcessor audioProcessor = new AudioProcessor(fs, frameLength, frameOverlap);

                    List<double[][]> correctMfccs = new ArrayList<>();
                    List<double[][]> incorrectMfccs = new ArrayList<>();
                    for (int j = 0; j < audioData.size(); j++) {
                        audioProcessor.setAudioData(audioData.get(j));
                        if (recordings.get(j).isCorrect()) {
                            correctMfccs.add(audioProcessor.computeMfccs(13));
                        } else {
                            incorrectMfccs.add(audioProcessor.computeMfccs(13));
                        }
                    }

                    /* Get all distances */
                    double[] correctDistances = getCorrectDistances(correctMfccs);
                    double[] incorrectDistances = getIncorrectDistances(correctMfccs, incorrectMfccs);

                    /* Write the data to disk */
                    FileManager.recreateDistanceDatFile(correctDistances, incorrectDistances, username, repetitionList.get(i).getWordName(), context);
                    int noCorrect = 0;
                    int noIncorrect = 0;
                    for (int j = 0; j < recordings.size(); j++) {
                        if (recordings.get(j).isCorrect()) {
                            FileManager.recreateMfccFile(correctMfccs.get(noCorrect), username,
                                    repetitionList.get(i).getWordName(), recordings.get(j).getRepetitionNumber(), context);
                            noCorrect++;
                        } else {
                            FileManager.recreateMfccFile(incorrectMfccs.get(noIncorrect), username,
                                    repetitionList.get(i).getWordName(), recordings.get(j).getRepetitionNumber(), context);
                            noIncorrect++;
                        }
                    }
                }
            }

            /* Write to file */
            FileManager.recreateRepetitionDatFile(allRecordings, username, context);
            FileManager.recreateWordsDatFile(wordViewModel.getAllWords(), username, context);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            this.exportOverlay.setVisibility(View.GONE);
            this.confirmationOverlay.setVisibility(View.VISIBLE);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
                    outAnimation.setDuration(200);
                    confirmationOverlay.setAnimation(outAnimation);
                    confirmationOverlay.setVisibility(View.GONE);
                }
            }, 2500);
        }

        private List<float[]> getAudioData(List<Recording> recordings, int frameLength, int padSize) {
            List<float[]> audioData = new ArrayList<>();
            for (int j = 0; j < recordings.size(); j++) {
                if (recordings.get(j).getFileLocation() != null) {
                    // Audio data is padded with frameLength / 2 on each side to center the frame
                    float[] unpaddedData = AudioProcessor.getWavAudioData(recordings.get(j).getFileLocation());
                    float[] paddedData = new float[unpaddedData.length + frameLength];
                    for (int k = 0; k < padSize; k++) {
                        paddedData[k] = unpaddedData[padSize + 1 - k];
                    }
                    for (int k = frameLength / 2; k < unpaddedData.length + padSize; k++) {
                        paddedData[k] = unpaddedData[k - padSize];
                    }
                    for (int k = 0; k < padSize; k++) {
                        paddedData[unpaddedData.length + padSize + k] = unpaddedData[unpaddedData.length - 1 - k];
                    }
                    audioData.add(paddedData);
                }
            }

            return audioData;
        }

        private double[] getCorrectDistances(List<double[][]> correctMfccs) {
            double[] correctScores = new double[correctMfccs.size() * (correctMfccs.size()-1)];
            // This will have duplicate scores because we compare i to j, then later j to i
            int idx = 0;
            for (int i = 0; i < correctMfccs.size(); i++) {
                for (int j = 0; j < correctMfccs.size(); j++) {
                    if (i != j) {
                        LibrosaStyleDTW dtw = new LibrosaStyleDTW(ignoreFirstMfcc(correctMfccs.get(i)), ignoreFirstMfcc(correctMfccs.get(j)));
                        correctScores[idx] = dtw.computeRMSE();
                        idx++;
                    }
                }
            }
            return correctScores;
        }

        private double[] getIncorrectDistances(List<double[][]> correctMfccs, List<double[][]> incorrectMfccs) {
            double[] incorrectScores = new double[incorrectMfccs.size() * correctMfccs.size()];
            int idx = 0;
            for (int i = 0; i < correctMfccs.size(); i++) {
                for (int j = 0; j < incorrectMfccs.size(); j++) {
                    LibrosaStyleDTW dtw = new LibrosaStyleDTW(ignoreFirstMfcc(correctMfccs.get(i)), ignoreFirstMfcc(incorrectMfccs.get(j)));
                    incorrectScores[idx] = dtw.computeRMSE();
                    idx++;
                }
            }
            return incorrectScores;
        }

        private double[][] ignoreFirstMfcc(double[][] mfcc) {
            double[][] newMfcc = new double[mfcc.length][mfcc[0].length-1];
            for (int i = 0; i < mfcc.length; i++) {
                for (int j = 0; j < mfcc[0].length-1; j++) {
                    newMfcc[i][j] = mfcc[i][j+1];
                }
            }
            return newMfcc;
        }

    }

    private static class zipAsyncTask extends AsyncTask<String, Void, Void> {
        /*
        Zip function taken from SO answer:
        https://stackoverflow.com/questions/6683600/zip-compress-a-folder-full-of-files-on-android
         */
        final int BUFFER = 2048;
        private Context mContext;
        private String destinationPath;
        private StorageReference storageReference;
        private RecordingViewModel recordingViewModel;
        private WordViewModel wordViewModel;
        private int userId;
        private String username;
        private FrameLayout overlay;
        private FrameLayout confirmationOverlay;

        zipAsyncTask(Context context, StorageReference reference, RecordingViewModel recordingViewModel,
                     WordViewModel wordViewModel, int userId, String username, FrameLayout overlay, FrameLayout confirmationOverlay) {
            mContext = context;
            storageReference = reference;
            this.recordingViewModel = recordingViewModel;
            this.userId = userId;
            this.username = username;
            this.wordViewModel = wordViewModel;
            this.overlay = overlay;
            this.confirmationOverlay = confirmationOverlay;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            overlay.setAnimation(inAnimation);
            overlay.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(final String... params) {
            // Get Repetitions to write label file
            List<Recording> recordings = recordingViewModel.getRecordingsListByUserId(userId);
            List<Word> words = wordViewModel.getAllWords();

            FileManager.recreateRepetitionDatFile(recordings, username, mContext);
            FileManager.recreateWordsDatFile(words, username, mContext);

            // Param 0 is source, param 1 is destination
            destinationPath = params[1];
            zipFileAtPath(params[0], params[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.d("Zipper", "Files zipped and attempting to upload");
            // Upload zip to Firebase upon zip completion
            Uri file = Uri.fromFile(new File(destinationPath));
            StorageReference zipRef = storageReference.child(username + String.valueOf(System.currentTimeMillis() / 1000) + ".zip");

            zipRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("Firebase", "File uploaded");
                            overlay.setVisibility(View.GONE);
                            confirmationOverlay.setVisibility(View.VISIBLE);

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
                                    outAnimation.setDuration(200);
                                    confirmationOverlay.setAnimation(outAnimation);
                                    confirmationOverlay.setVisibility(View.GONE);
                                }
                            }, 2500);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Firebase", "File not uploaded");
                            overlay.setVisibility(View.GONE);
                            Toast.makeText(mContext, "Files not able to upload!", Toast.LENGTH_LONG).show();
                        }
                    });
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
