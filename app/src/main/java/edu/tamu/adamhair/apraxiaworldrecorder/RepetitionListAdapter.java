package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RecordingViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;

/**
 * Created by adamhair on 4/9/2018.
 */

public class RepetitionListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private ArrayList<Recording> dataSource;
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

    public RepetitionListAdapter(Context context, ArrayList<Recording> items, TextView correctTextView, TextView incorrectTextView) {
        mContext = context;
        dataSource = items;
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        recording = false;
        this.correctTextView = correctTextView;
        this.incorrectTextView = incorrectTextView;

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
            holder.recordingPath = recordingPrefix + "/Calibration/" + word + "/" + word +
                    "_" + String.valueOf(getItem(position).getRepetitionNumber()) + ".wav";
            holder.wavRecorder = new WavRecorder(holder.recordingPath);

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

                        holder.wavRecorder.startRecording();

                    } else {
                        recordButton.setText("Rec");
                        recording = false;
                        recordingRep = -1;

                        // Stop recording and notify the file system to scan the new file for USB access
                        holder.wavRecorder.stopRecording();
                        if (getItem(position).getFileLocation() == null) {
                            if (getItem(position).isCorrect()) {
                                correctCount++;
                            } else {
                                incorrectCount++;
                            }
                            repetitionViewModel.updateWordLabelCounts(getItem(position).getWordId(), getItem(position).getUserId(), correctCount, incorrectCount);
                        }
                        getItem(position).setFileLocation(holder.recordingPath);
                        recordingViewModel.updateRecordings(getItem(position));
                        Log.d("Recording", getItem(position).getFileLocation());
                        MediaScannerConnection.scanFile(mContext, new String[] {holder.recordingPath}, null, null);
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
                    try {
                        MediaPlayer mp = new MediaPlayer();
                        mp.setDataSource(holder.recordingPath);
                        mp.prepare();
                        mp.start();
                    } catch (IOException e) {
                        Log.e("MediaPlayer", "Unable to play recording");
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

    public void setWord(String word) {
        this.word = word;
    }

    public void setUsername(String username) {
        this.username = username;
        recordingPrefix = FileManager.getUserFolderString(username);
    }


    public void addItems(ArrayList<Recording> recordings) {
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
        public WavRecorder wavRecorder;
        public String recordingPath;
    }

}
