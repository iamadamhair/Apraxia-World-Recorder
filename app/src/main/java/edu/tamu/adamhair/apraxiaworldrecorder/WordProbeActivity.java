package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.asynctasks.ZipAsyncTask;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Probe;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.ProbeViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;

public class WordProbeActivity extends AppCompatActivity {

    ProbeViewModel probeViewModel;
    RepetitionViewModel repetitionViewModel;

    String username;
    int userId;
    boolean newProbe;
    int probeNum;

    /* Firebase variables */
    StorageReference storageReference;

    /* UI elements */
    ListView probeWordsListView;
    Button probeRecordButton;
    Button probePlayButton;
    RadioGroup probeLabelRadioGroup;
    RadioButton probeCorrectRadioButton;
    RadioButton probeIncorrectRadioButton;
    Button probeOkButton;
    ImageView probeImageView;
    TextView probeWordTextView;
    FrameLayout probeFrameLayout;
    Button probeUploadButton;
    String dateString = null;

    /* ListView items */
    ProbeWordListAdapter probeWordListAdapter;
    List<Probe> dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_probe);

        storageReference = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        userId = intent.getIntExtra("userId", 0);
        newProbe = intent.getBooleanExtra("newProbe", true);
        probeNum = intent.getIntExtra("probeNum", -1);

        /* Link UI elements */
        probeWordsListView = findViewById(R.id.probeWordsListView);
        probeRecordButton = findViewById(R.id.probeRecordButton);
        probePlayButton = findViewById(R.id.probePlayButton);
        probeLabelRadioGroup = findViewById(R.id.probeLabelRadioGroup);
        probeCorrectRadioButton = findViewById(R.id.probeCorrectRadioButton);
        probeIncorrectRadioButton = findViewById(R.id.probeIncorrectRadioButton);
        probeOkButton = findViewById(R.id.probeOkButton);
        probeImageView = findViewById(R.id.probeImageView);
        probeWordTextView = findViewById(R.id.probeWordTextView);
        probeFrameLayout = findViewById(R.id.probeFrameLayout);
        probeUploadButton = findViewById(R.id.uploadProbeButton);

        probeFrameLayout.setVisibility(View.INVISIBLE);

        /* Set up ListView */
        dataSource = new ArrayList<>();
        probeWordListAdapter = new ProbeWordListAdapter(getApplication(), dataSource, username);
        probeWordListAdapter.setUiElements(probeFrameLayout, probeWordTextView,
                probeLabelRadioGroup, probeCorrectRadioButton, probeIncorrectRadioButton,
                probeImageView, probeOkButton, probePlayButton, probeRecordButton);
        probeWordsListView.setAdapter(probeWordListAdapter);

        /* Set upLiveData */
        probeViewModel = ViewModelProviders.of(this).get(ProbeViewModel.class);
        probeWordListAdapter.setProbeViewModel(probeViewModel);
        repetitionViewModel = ViewModelProviders.of(this).get(RepetitionViewModel.class);
        if (newProbe) {
            probeViewModel.createNewProbe(userId, probeWordListAdapter);
        }

        if (probeNum >= 0) {
            probeViewModel.getProbesForNumber(probeNum).observe(WordProbeActivity.this, new Observer<List<Probe>>() {
                @Override
                public void onChanged(@Nullable List<Probe> probes) {
                    probeWordListAdapter.addItems(probes);
                    if (probes.size() > 0) {
                        dateString = probes.get(0).getProbeDate();
                    }
                }
            });
        }

        /* Set onClick listener */
        probeUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (probeWordListAdapter.probesCompleted()) {
                    if (dateString != null) {
                        String[] params = new String[2];
                        params[0] = FileManager.getProbeDateFolder(username, dateString).toString();
                        params[1] = Environment.getExternalStorageDirectory().toString() + "/probe " +
                            username + " " + dateString + ".zip";
                        //new ZipAsyncTask(WordProbeActivity.this, storageReference, )
                    }


                } else {
                    Toast.makeText(WordProbeActivity.this, "You need to record all words before uploading", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        probeWordListAdapter.cleanup();
    }

    @Override
    public void onResume() {
        super.onResume();
        probeWordListAdapter.resume();
    }
}
