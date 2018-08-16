package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

public class WordProbeActivity extends AppCompatActivity {

    String username;
    int userId;

    /* UI elements */
    ListView probeWordsListView;
    Button probeRecordButton;
    Button probePlayButton;
    RadioGroup probeLabelRadioGroup;
    Button probeOkButton;
    ImageView probeImageView;
    TextView probeWordTextView;
    FrameLayout probeFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_probe);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        userId = intent.getIntExtra("userId", 0);

        /* Link UI elements */
        probeWordsListView = findViewById(R.id.probeWordsListView);
        probeRecordButton = findViewById(R.id.probeRecordButton);
        probePlayButton = findViewById(R.id.probePlayButton);
        probeLabelRadioGroup = findViewById(R.id.probeLabelRadioGroup);
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
    }
}
