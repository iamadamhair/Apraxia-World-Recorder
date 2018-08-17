package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

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

    /* ListView items */
    ProbeWordListAdapter probeWordListAdapter;
    List<Probe> dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_probe);

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

        /* Set onClick listeners */
        probeOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // SAVE EVERYTHING TO DB BEFORE DISMISSING
                probeFrameLayout.setVisibility(View.GONE);
            }
        });

        probeFrameLayout.setVisibility(View.GONE);

        /* Set up ListView */
        dataSource = new ArrayList<>();
        probeWordListAdapter = new ProbeWordListAdapter(getApplication(), dataSource, username);
        probeWordListAdapter.setUiElements(probeFrameLayout, probeWordTextView, probeLabelRadioGroup, probeImageView);
        probeWordsListView.setAdapter(probeWordListAdapter);

        /* Set upLiveData */
        probeViewModel = ViewModelProviders.of(this).get(ProbeViewModel.class);
        repetitionViewModel = ViewModelProviders.of(this).get(RepetitionViewModel.class);
        if (newProbe) {
            probeViewModel.createNewProbe(userId, probeWordListAdapter);
        }

        if (probeNum >= 0) {
            probeViewModel.getProbesForNumber(probeNum).observe(WordProbeActivity.this, new Observer<List<Probe>>() {
                @Override
                public void onChanged(@Nullable List<Probe> probes) {
                    probeWordListAdapter.addItems(probes);
                }
            });
        }
    }
}
