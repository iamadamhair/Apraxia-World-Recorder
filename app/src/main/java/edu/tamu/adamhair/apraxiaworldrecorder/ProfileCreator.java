package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ProfileCreator extends AppCompatActivity {

    Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creator);

        setTitle("Create Apraxia World profile");

        /* Link UI elements */
        createButton = (Button) findViewById(R.id.createButton);

        /* Add onclick listners */
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {switchToWordSelectionActivity(view);};
        });
    }

    private void switchToWordSelectionActivity(View view) {
        Intent intent = new Intent(this, WordSelectionActivity.class);
        startActivity(intent);
    }
}
