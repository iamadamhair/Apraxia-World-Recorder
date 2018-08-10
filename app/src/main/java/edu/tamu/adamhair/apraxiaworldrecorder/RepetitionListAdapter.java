package edu.tamu.adamhair.apraxiaworldrecorder;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.audio.AudioProcessor;
import edu.tamu.adamhair.apraxiaworldrecorder.audio.LibrosaStyleDTW;
import edu.tamu.adamhair.apraxiaworldrecorder.audio.WavRecorder;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RecordingViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;

/**
 * Created by adamhair on 4/9/2018.
 */

public class RepetitionListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private ArrayList<Recording> dataSource;
    private ArrayList<WavRecorder> wavRecorders;
    private ArrayList<MediaPlayer> mediaPlayers;
    private ArrayList<String> recordingPaths;
    private ArrayList<Handler> handlers;
    private String username;
    private boolean recording;
    private int recordingRep = -1;
    private String recordingPrefix;
    private String word;
    private RecordingViewModel recordingViewModel;
    private RepetitionViewModel repetitionViewModel;
    private int correctCount;
    private int incorrectCount;
    private TextView correctTextView;
    private TextView incorrectTextView;
    private Button mfccButton;

    public RepetitionListAdapter(Context context, ArrayList<Recording> items, TextView correctTextView, TextView incorrectTextView, Button mfccButton) {
        mContext = context;
        dataSource = items;
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        recording = false;
        this.correctTextView = correctTextView;
        this.incorrectTextView = incorrectTextView;
        this.mfccButton = mfccButton;

        correctCount = 0;
        incorrectCount = 0;
        for (int i = 0; i < dataSource.size(); i++) {
            if (dataSource.get(i).isCorrect() && dataSource.get(i).getFileLocation() != null) {
                correctCount++;
            } else if (!dataSource.get(i).isCorrect() && dataSource.get(i).getFileLocation() != null) {
                incorrectCount++;
            }
        }

        this.correctTextView.setText("Correct: " + String.valueOf(correctCount));
        this.incorrectTextView.setText("Incorrect: " + String.valueOf(incorrectCount));

        if (correctCount == 5 && incorrectCount == 5) {
            mfccButton.setEnabled(true);
        } else {
            mfccButton.setEnabled(false);
        }

        // Initialize empty lists to be filled in when the new data comes
        recordingPaths = new ArrayList<>();
        wavRecorders = new ArrayList<>();
        mediaPlayers = new ArrayList<>();
        handlers = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Recording getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final RepetitionViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.repetition_list_layout, parent, false);

            holder = new RepetitionViewHolder();

            holder.countTextView = (TextView) convertView.findViewById(R.id.repetitionCountTextView);
            holder.correctRadioButton = (RadioButton) convertView.findViewById(R.id.correctRadioButton);
            holder.incorrectRadioButton = (RadioButton) convertView.findViewById(R.id.incorrectRadioButton);
            holder.radioGroup = (RadioGroup) convertView.findViewById(R.id.recordingLabelRadioGroup);
            holder.recordButton = (Button) convertView.findViewById(R.id.repetitionRecordButton);
            holder.playButton = (Button) convertView.findViewById(R.id.repetitionPlayButton);

            convertView.setTag(holder);
        } else {
            holder = (RepetitionViewHolder) convertView.getTag();
        }


        TextView countTextView = holder.countTextView;
        countTextView.setText("Rep " + String.valueOf(getItem(position).getRepetitionNumber()));

        final Button recordButton = holder.recordButton;
        recordButton.setText("Rec");

        final Button playButton = holder.playButton;
        if (TextUtils.isEmpty(getItem(position).getFileLocation())) {
            playButton.setEnabled(false);
            if (Build.VERSION.SDK_INT >= 21) {
                playButton.setBackgroundTintList(mContext.getResources().getColorStateList(android.R.color.darker_gray));
            }
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                playButton.setBackgroundTintList(mContext.getResources().getColorStateList(android.R.color.holo_green_light));
            }
            playButton.setEnabled(true);
        }

        /* Set up the radio buttons */
        final RadioButton correctRadioButton = holder.correctRadioButton;
        RadioButton incorrectRadioButton = holder.incorrectRadioButton;
        RadioGroup radioGroup = holder.radioGroup;

        RadioGroup.OnCheckedChangeListener changeListener =  new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                getItem(position).setCorrect(correctRadioButton.isChecked());
                if(getItem(position).getFileLocation() != null) {
                    recordingViewModel.updateRecordings(getItem(position));
                    if (getItem(position).isCorrect()) {
                        correctCount++;
                        incorrectCount--;
                    } else {
                        correctCount--;
                        incorrectCount++;
                    }
                    if (correctCount == 5 && incorrectCount == 5) {
                        mfccButton.setEnabled(true);
                    } else {
                        mfccButton.setEnabled(false);
                    }
                    correctTextView.setText("Correct: " + String.valueOf(correctCount));
                    incorrectTextView.setText("Incorrect: " + String.valueOf(incorrectCount));
                    repetitionViewModel.updateWordLabelCounts(getItem(position).getWordId(), getItem(position).getUserId(), correctCount, incorrectCount);
                }
            }
        };

        // Have to disable the listener because setting the checked will trigger it
        if (getItem(position).isCorrect()) {
            radioGroup.setOnCheckedChangeListener(null);
            correctRadioButton.setChecked(true);
            radioGroup.setOnCheckedChangeListener(changeListener);
        } else {
            radioGroup.setOnCheckedChangeListener(null);
            incorrectRadioButton.setChecked(true);
            radioGroup.setOnCheckedChangeListener(changeListener);
        }

        /* Set on onClickListener */
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Only record if no other item is recording
                if (recordingRep == -1 || recordingRep == position) {
                    if (!recording) {
                        recordButton.setText("Stop");
                        recording = true;
                        recordingRep = position;

                        wavRecorders.get(position).startRecording();
                        // Set timeout on recording
                        handlers.get(position).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("Recorder", "Timed out");
                                stopRecording(position, recordButton, playButton);
                            }
                        }, 10000);
                    } else {
                        stopRecording(position, recordButton, playButton);
                        handlers.get(position).removeCallbacksAndMessages(null);
                    }
                }

            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Only play if not recording
                if (recordingRep == -1) {
                    if (!mediaPlayers.get(position).isPlaying()) {
                        try {
                            mediaPlayers.get(position).reset();
                            mediaPlayers.get(position).setDataSource(recordingPaths.get(position));
                            mediaPlayers.get(position).prepare();
                            mediaPlayers.get(position).start();
                        } catch(Exception e) {
                            Log.e("MediaPlayer", "Unable to play recording");
                        }
                    }
                }
            }
        });

        return convertView;
    }

    private void stopRecording(int position, Button recordButton, Button playButton) {
        recordButton.setText("Rec");
        recording = false;
        recordingRep = -1;

        // Stop recording and notify the file system to scan the new file for USB access
        wavRecorders.get(position).stopRecording();
        if (getItem(position).getFileLocation() == null) {
            if (getItem(position).isCorrect()) {
                correctCount++;
            } else {
                incorrectCount++;
            }
            repetitionViewModel.updateWordLabelCounts(getItem(position).getWordId(), getItem(position).getUserId(), correctCount, incorrectCount);
        }
        getItem(position).setFileLocation(recordingPaths.get(position));
        recordingViewModel.updateRecordings(getItem(position));
        Log.d("Recording", getItem(position).getFileLocation());
        MediaScannerConnection.scanFile(mContext, new String[]{recordingPaths.get(position)}, null, null);
        playButton.setEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            playButton.setBackgroundTintList(mContext.getResources().getColorStateList(android.R.color.holo_green_light));
        }
    }

    public void setRepetitionViewModel(RepetitionViewModel repetitionViewModel) {
        this.repetitionViewModel = repetitionViewModel;
    }

    public void setRecordingViewModel(RecordingViewModel recordingViewModel) {
        this.recordingViewModel = recordingViewModel;
    }

    public void releaseMediaPlayers() {
        for (int i = 0; i < mediaPlayers.size(); i++) {
            if (mediaPlayers.get(i) != null) {
                if (mediaPlayers.get(i).isPlaying()) {
                    mediaPlayers.get(i).stop();
                }
                mediaPlayers.get(i).release();
            }
        }
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setUsername(String username) {
        this.username = username;
        recordingPrefix = FileManager.getUserFolderString(username);
    }

    public void addItems(ArrayList<Recording> recordings) {
        // This is called when we modify things in the database?
        this.dataSource = recordings;
        notifyDataSetChanged();

        for (int i = 0; i < mediaPlayers.size(); i++) {
            if (mediaPlayers.get(i) != null) {
                mediaPlayers.get(i).release();
            }
        }

        for (int i = 0; i < handlers.size(); i++) {
            handlers.get(i).removeCallbacksAndMessages(null);
        }


        // Start with empty arraylists
        recordingPaths.clear();
        wavRecorders.clear();
        mediaPlayers.clear();
        handlers.clear();

        correctCount = 0;
        incorrectCount = 0;
        for (int i = 0; i < dataSource.size(); i++) {
            if (dataSource.get(i).isCorrect() && dataSource.get(i).getFileLocation() != null) {
                correctCount++;
            } else if (!dataSource.get(i).isCorrect() && dataSource.get(i).getFileLocation() != null) {
                incorrectCount++;
            }
            recordingPaths.add(recordingPrefix + "/Calibration/" + word + "/" + word +
                    "_" + String.valueOf(getItem(i).getRepetitionNumber()) + ".wav");
            wavRecorders.add(new WavRecorder(recordingPaths.get(i)));
//            wavRecorders.get(i).setUsePreemphasis(true);
            mediaPlayers.add(new MediaPlayer());
            try {
                File file = new File(recordingPaths.get(i));
                if (file.isFile()) {
                    mediaPlayers.get(i).setDataSource(recordingPaths.get(i));
                }
            } catch(Exception e) {
                e.printStackTrace();
                Log.e("MediaPlayer", "Unable to set media path for " + String.valueOf(i));
            }
            handlers.add(new Handler());
        }

        if (correctCount == 5 && incorrectCount == 5) {
            mfccButton.setEnabled(true);
        } else {
            mfccButton.setEnabled(false);
        }
        correctTextView.setText("Correct: " + String.valueOf(correctCount));
        incorrectTextView.setText("Incorrect: " + String.valueOf(incorrectCount));
    }

    private static class RepetitionViewHolder {
        public TextView countTextView;
        public RadioButton correctRadioButton;
        public RadioButton incorrectRadioButton;
        public RadioGroup radioGroup;
        public Button recordButton;
        public Button playButton;
    }

    public double getEffectSize() {
        int fs = 16000;
        int frameLength = 512;
        int frameOverlap = 128;
        int padSize = frameLength / 2;

        List<float[]> audioData = new ArrayList<>();
        for (int i = 0; i < dataSource.size(); i++) {
            if (dataSource.get(i).getFileLocation() != null) {
                // Audio data is padded with frameLength / 2 on each side to center the frame
                float[] unpaddedData = AudioProcessor.getWavAudioData(dataSource.get(i).getFileLocation());
                float[] paddedData = new float[unpaddedData.length + frameLength];
                for (int j = 0; j < padSize; j++) {
                    paddedData[j] = unpaddedData[padSize + 1 - j];
                }
                for (int j = frameLength / 2; j < unpaddedData.length + padSize; j++) {
                    paddedData[j] = unpaddedData[j - padSize];
                }
                for (int j = 0; j < padSize; j++) {
                    paddedData[unpaddedData.length + padSize + j] = unpaddedData[unpaddedData.length - 1 - j];
                }
                audioData.add(paddedData);
            }
        }

        AudioProcessor audioProcessor = new AudioProcessor(fs, frameLength, frameOverlap);

        List<double[][]> mfccs = new ArrayList<>();
        for (int i = 0; i < audioData.size(); i++) {
            Log.i("Audio len", String.valueOf(i+1) + " " + String.valueOf(audioData.get(i).length));
            audioProcessor.setAudioData(audioData.get(i));
            mfccs.add(audioProcessor.computeMfccs(13));
        }

        List<double[][]> correctMfccs = new ArrayList<>();
        List<double[][]> incorrectMfccs = new ArrayList<>();
        for (int i = 0; i < audioData.size(); i++) {
            audioProcessor.setAudioData(audioData.get(i));
            if (dataSource.get(i).isCorrect()) {
                correctMfccs.add(audioProcessor.computeMfccs(13));
            } else {
                incorrectMfccs.add(audioProcessor.computeMfccs(13));
            }
        }

        double[] correctScores = new double[correctMfccs.size() * (correctMfccs.size()-1)];
        // This will have duplicate scores because we compare i to j, then later j to i
        int idx = 0;
        for (int i = 0; i < correctMfccs.size(); i++) {
            Log.i("Correct MFCC len", String.valueOf(i+1) + " is " + String.valueOf(correctMfccs.get(i).length));
            for (int j = 0; j < correctMfccs.size(); j++) {
                if (i != j) {
                    LibrosaStyleDTW dtw = new LibrosaStyleDTW(ignoreFirstMfcc(correctMfccs.get(i)), ignoreFirstMfcc(correctMfccs.get(j)));
                    writePathToFile(dtw.getWarpingPath(), i, j, "c");
                    correctScores[idx] = Math.log10(dtw.computeRMSE());
                    idx++;
                }
            }
        }

        double[] incorrectScores = new double[incorrectMfccs.size() * correctMfccs.size()];
        idx = 0;
        for (int i = 0; i < correctMfccs.size(); i++) {
            for (int j = 0; j < incorrectMfccs.size(); j++) {
                Log.i("Incorrect MFCC len", String.valueOf(j+1) + " is " + String.valueOf(incorrectMfccs.get(j).length));
                LibrosaStyleDTW dtw = new LibrosaStyleDTW(ignoreFirstMfcc(correctMfccs.get(i)), ignoreFirstMfcc(incorrectMfccs.get(j)));
                writePathToFile(dtw.getWarpingPath(), i, j, "i");
                incorrectScores[idx] = Math.log10(dtw.computeRMSE());
                idx++;
            }
        }

        return audioProcessor.computeEffectSize(correctScores, incorrectScores);
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

    private void writePathToFile(int[][] path, int recording1, int recording2, String correct) {
        File pathData = new File(Environment.getExternalStorageDirectory().toString(),
                correct + "-" + String.valueOf(recording1) + "-" + String.valueOf(recording2) + "-path.txt");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(pathData);
            for (int i = 0; i < path.length; i++) {
                fileOutputStream.write((String.valueOf(path[i][0]) + " " +
                        String.valueOf(path[i][1]) + "\n").getBytes());
            }
            fileOutputStream.close();
            pathData.setReadable(true);
            MediaScannerConnection.scanFile(mContext, new String[]{pathData.toString()}, null, null);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
