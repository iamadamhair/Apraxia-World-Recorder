package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.audio.WavRecorder;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Probe;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.ProbeViewModel;

public class ProbeWordListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private List<Probe> dataSource;
    private String username;
    private ProbeViewModel probeViewModel;

    // UI elements
    private FrameLayout frameLayout;
    private TextView probeWord;
    private RadioGroup probeLabel;
    private RadioButton probeCorrectRadioButton;
    private RadioButton probeIncorrectRadioButton;
    private ImageView probeImage;
    private Button probeOkButton;
    private Button playButton;
    private Button recordButton;
    private Button uploadButton;

    // Recording audio
    private WavRecorder wavRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    Handler handler;

    public ProbeWordListAdapter(Context context, List<Probe> dataSource,
                                String username) {
        mContext = context;
        this.dataSource = dataSource;
        this.username = username;
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
    }

    public void setProbeViewModel(ProbeViewModel probeViewModel) {
        this.probeViewModel = probeViewModel;
    }

    public void setUiElements(FrameLayout frameLayout, TextView probeWord, RadioGroup probeLabel,
                              RadioButton probeCorrectRadioButton, RadioButton probeIncorrectRadioButton,
                              ImageView probeImage, Button probeOkButton,
                              Button playButton, Button recordButton) {
        this.frameLayout = frameLayout;
        this.probeWord = probeWord;
        this.probeLabel = probeLabel;
        this.probeCorrectRadioButton = probeCorrectRadioButton;
        this.probeIncorrectRadioButton = probeIncorrectRadioButton;
        this.probeImage = probeImage;
        this.probeOkButton = probeOkButton;
        this.playButton = playButton;
        this.recordButton = recordButton;
    }

    public boolean probesCompleted() {
        if (dataSource.size() == 0) {
            return false;
        }
        int probesWithFilePaths = 0;
        for (int i = 0; i < dataSource.size(); i++) {
            if (!TextUtils.isEmpty(getItem(i).getFileLocation()))
                probesWithFilePaths++;
        }
        return probesWithFilePaths == dataSource.size();
    }

    public void addItems(List<Probe> probes) {
        this.dataSource = probes;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Probe getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.probe_selection_layout, parent, false);

            holder = new ViewHolder();

            holder.probeNameTextView = convertView.findViewById(R.id.probeNameTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TextView probeNameTextView = holder.probeNameTextView;
        String title = getItem(position).getWordName();
        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        probeNameTextView.setText(title);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFrameView(position);
            }
        });

        return convertView;
    }


    private void showFrameView(final int position) {
        int imageId = mContext.getResources().getIdentifier("edu.tamu.adamhair.apraxiaworldrecorder:drawable/" + getItem(position).getWordName(), null, null);
        this.probeImage.setImageResource(imageId);

        String title = getItem(position).getWordName();
        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        this.probeWord.setText(title);

        if (TextUtils.isEmpty(getItem(position).getFileLocation())) {
            playButton.setEnabled(false);
            if (Build.VERSION.SDK_INT >= 21) {
                playButton.setBackgroundTintList(mContext.getResources().getColorStateList(android.R.color.darker_gray));
            }
        } else {
            playButton.setEnabled(true);
            if (Build.VERSION.SDK_INT >= 21) {
                playButton.setBackgroundTintList(mContext.getResources().getColorStateList(android.R.color.holo_green_light));
            }
        }

        recordButton.setText("Rec");
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleRecording(position);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording && !mediaPlayer.isPlaying()) {
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(getItem(position).getFileLocation());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        RadioGroup.OnCheckedChangeListener changeListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                getItem(position).setCorrect(probeCorrectRadioButton.isChecked());
            }
        };

        if (getItem(position).isCorrect()) {
            probeLabel.setOnCheckedChangeListener(null);
            probeCorrectRadioButton.setChecked(true);
            probeLabel.setOnCheckedChangeListener(changeListener);
        } else {
            probeLabel.setOnCheckedChangeListener(null);
            probeIncorrectRadioButton.setChecked(true);
            probeLabel.setOnCheckedChangeListener(changeListener);
        }

        probeOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                probeViewModel.updateProbes(getItem(position));
                frameLayout.setVisibility(View.INVISIBLE);
            }
        });

        frameLayout.setVisibility(View.VISIBLE);
    }

    private void handleRecording(final int position) {
        String dateString = getItem(position).getProbeDate();
        String filename = FileManager.getProbeFolder(username).toString() + "/" +
                dateString + "/" + getItem(position).getWordName() + ".wav";

        if (isRecording) {
            wavRecorder.stopRecording();
            handler.removeCallbacksAndMessages(null);
            wavRecorder = null;

            recordButton.setText("Rec");
            isRecording = false;

            getItem(position).setFileLocation(filename);
            playButton.setEnabled(true);
            if (Build.VERSION.SDK_INT >= 21) {
                playButton.setBackgroundTintList(mContext.getResources().getColorStateList(android.R.color.holo_green_light));
            }

            MediaScannerConnection.scanFile(mContext, new String[]{filename}, null, null);
            probeViewModel.updateProbes(getItem(position));
        } else if (!mediaPlayer.isPlaying()){
            // Check that the necessary folders exists
            if (!FileManager.probeFolderExists(username))
                FileManager.createProbeFolder(username);
            if (!FileManager.probeDateFolderExists(username, dateString))
                FileManager.createProbeDateFolder(username, dateString);

            recordButton.setText("Stop");
            isRecording = true;

            wavRecorder = new WavRecorder(filename);
            wavRecorder.startRecording();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handleRecording(position);
                }
            }, 10000);
        }
    }

    public void cleanup() {
        if (wavRecorder != null) {
            wavRecorder.stopRecording();
        }
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        handler.removeCallbacksAndMessages(null);
    }

    public void resume() {
        mediaPlayer = new MediaPlayer();
    }

    private static class ViewHolder {
        public TextView probeNameTextView;
    }
}
