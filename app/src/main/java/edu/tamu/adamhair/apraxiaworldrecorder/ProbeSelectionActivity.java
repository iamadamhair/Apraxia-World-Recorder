package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.Probe;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.ProbeViewModel;

public class ProbeSelectionActivity extends AppCompatActivity {

    ProbeViewModel probeViewModel;
    int userId;
    String username;

    /* UI elements */
    Button newProbeButton;
    ListView existingProbesListView;

    /* ListView items */
    ExistingProbeListAdapter adapter;
    List<Probe> probeList;

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

        /* Set up ListView */
        probeList = new ArrayList<>();
        adapter = new ExistingProbeListAdapter(getApplication(), probeList, username);
        existingProbesListView.setAdapter(adapter);

        /* Set up LiveData */
        probeViewModel = ViewModelProviders.of(this).get(ProbeViewModel.class);
        probeViewModel.getUniqueProbesForUser(userId).observe(ProbeSelectionActivity.this, new Observer<List<Probe>>() {
            @Override
            public void onChanged(@Nullable List<Probe> probes) {
                adapter.addItems(probes);
            }
        });
    }

    private void launchNewProbeActivity(View view) {
        Intent intent = new Intent(this, WordProbeActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("userId", userId);
        intent.putExtra("newProbe", true);
        startActivity(intent);
    }
}
