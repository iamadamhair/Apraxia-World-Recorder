package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.audio.AudioFeatureExtractor;
import edu.tamu.adamhair.apraxiaworldrecorder.audio.DTW;
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
    private ArrayList<MediaRecorder> mediaRecorders;
    private ArrayList<MediaPlayer> mediaPlayers;
    private ArrayList<String> recordingPaths;
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
        mediaRecorders = new ArrayList<>();
        mediaPlayers = new ArrayList<>();
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
            playButton.setBackgroundTintList(mContext.getResources().getColorStateList(android.R.color.darker_gray));
        } else {
            playButton.setBackgroundTintList(mContext.getResources().getColorStateList(android.R.color.holo_green_light));
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
                        try {
//                            mediaRecorders.get(position).prepare();
//                            mediaRecorders.get(position).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Media Recorder", "Unable to prepare and start media recorder");
                        }

                    } else {
                        recordButton.setText("Rec");
                        recording = false;
                        recordingRep = -1;

                        // Stop recording and notify the file system to scan the new file for USB access
                        wavRecorders.get(position).stopRecording();
//                        mediaRecorders.get(position).stop();
//                        mediaRecorders.get(position).release();
                        // Recreate the mediarecorder after using it
//                        mediaRecorders.set(position, configureMediaRecorder(recordingPaths.get(position)));
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
                        MediaScannerConnection.scanFile(mContext, new String[] {recordingPaths.get(position)}, null, null);
                        playButton.setEnabled(true);
                        playButton.setBackgroundTintList(mContext.getResources().getColorStateList(android.R.color.holo_green_light));
                    }
                }

            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Only play if not recording
                if (recordingRep == -1) {
//                    try {
//                        MediaPlayer mp = new MediaPlayer();
//                        mp.setDataSource(recordingPaths.get(position));
//                        mp.prepare();
//                        mp.start();
//                    } catch (IOException e) {
//                        Log.e("MediaPlayer", "Unable to play recording");
//                    }
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

    public void setRepetitionViewModel(RepetitionViewModel repetitionViewModel) {
        this.repetitionViewModel = repetitionViewModel;
    }

    public void setRecordingViewModel(RecordingViewModel recordingViewModel) {
        this.recordingViewModel = recordingViewModel;
    }

    public void releaseMediaRecorders() {
        if (recordingRep != -1) {
            mediaRecorders.get(recordingRep).stop();
        }
        for (int i = 0; i < mediaRecorders.size(); i++) {
            if(mediaRecorders.get(i) != null) {
                mediaRecorders.get(i).release();
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
        Log.d("AddItems", "Calling add items function");
        this.dataSource = recordings;
        notifyDataSetChanged();

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
//            mediaRecorders.add(configureMediaRecorder(recordingPaths.get(i)));
            mediaPlayers.add(new MediaPlayer());
            try {
                mediaPlayers.get(i).setDataSource(recordingPaths.get(i));
            } catch(Exception e) {
                Log.e("MediaPlayer", "Unable to set media path");
            }
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

    private MediaRecorder configureMediaRecorder(String path) {
        int fs = 16000;
        int bitDepth = 16;

        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioEncodingBitRate(bitDepth * fs);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioSamplingRate(fs);
        mediaRecorder.setOutputFile(path);
        return mediaRecorder;
    }

    public void runMfccProcessing() {

        List<List<float[]>> allMfccs = new ArrayList<>();
        AudioFeatureExtractor featureExtractor;
        for (int i = 0; i < dataSource.size(); i++) {
            if (dataSource.get(i).getFileLocation() != null) {
                featureExtractor = new AudioFeatureExtractor(dataSource.get(i).getFileLocation(), 16000, mContext);
                featureExtractor.generateFeatures();
                allMfccs.add(featureExtractor.getMfccList());
                Log.d("Mfccs", "Mfcc length: " + String.valueOf(allMfccs.get(i).size()));
                Log.d("Mfccs", "Extracted features for " + String.valueOf(dataSource.get(i).getRepetitionNumber()));
            }
        }

        for (int i = 0; i < allMfccs.size(); i++) {
            for (int j = i+1; j < allMfccs.size(); j++) {
                DTW.Result dtwResult = DTW.computeMfccDtw(allMfccs.get(i), allMfccs.get(j));
                Log.d("RMSE", String.valueOf(i) + " & " + String.valueOf(j) + ": " +
                String.valueOf(computeRMSE(allMfccs.get(i), allMfccs.get(j), dtwResult.getWarpingPath())));
            }
        }
    }

    private double computeRMSE(List<float[]> mfcc1, List<float[]> mfcc2, int[][] warp) {
        double rmse = 0;
        int noCoefficients = mfcc1.get(0).length;

        for (int i = 0; i < warp.length; i++) {
            for (int j = 0; j < noCoefficients; j++) {
                rmse += Math.pow(mfcc1.get(warp[i][0])[j] - mfcc2.get(warp[i][1])[j], 2);
            }
        }
        return Math.sqrt(rmse)/warp.length;
    }

}
