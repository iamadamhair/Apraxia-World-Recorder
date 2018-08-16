package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class ProbeSelectionActivity extends AppCompatActivity {

    int userId;
    String username;

    /* UI elements */
    Button newProbeButton;
    ListView existingProbesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_probe_selection);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        userId = intent.getIntExtra("userId", 0);

        /* Link UI elements */
        newProbeButton = findViewById(R.id.newProbeButton);
        existingProbesListView = findViewById(R.id.existingProbesListView);

        /* Set onClickListeners */
        newProbeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchNewProbeActivity(view);
            }
        });

        /* Set up LiveData */
        // Fill in probe list here once DB is set up
    }

    private void launchNewProbeActivity(View view) {
        Intent intent = new Intent(this, WordProbeActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
}
