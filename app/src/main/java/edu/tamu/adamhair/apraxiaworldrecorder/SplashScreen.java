package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    /* UI elements */
    TextView instructionsTextView;
    Button createProfileButton;
    Button enterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        /* Set all UI elements */
        instructionsTextView = (TextView) findViewById(R.id.instructionsTextView);
        createProfileButton = (Button) findViewById(R.id.createProfileButton);
        enterButton = (Button) findViewById(R.id.enterButton);

        /* Set onClick listeners */
        instructionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {switchToInstructionActivity(view);}
        });
        createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {switchToCreateProfileActivity(view);}
        });
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {switchToWordSelectionActivity(view);}
        });
    }

    private void switchToInstructionActivity(View view) {
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }

    private void switchToCreateProfileActivity(View view) {
        Intent intent = new Intent(this, ProfileCreator.class);
        startActivity(intent);
    }

    private void switchToWordSelectionActivity(View view) {
        Intent intent = new Intent(this, WordSelectionActivity.class);
        startActivity(intent);
    }
}
