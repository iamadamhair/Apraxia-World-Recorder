package edu.tamu.adamhair.apraxiaworldrecorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SplashScreen extends AppCompatActivity {

    static AppDatabase appDatabase;

    /* UI elements */
    TextView instructionsTextView;
    Button createProfileButton;
    Button enterButton;
    Spinner userSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        /* Set all UI elements */
        instructionsTextView = (TextView) findViewById(R.id.instructionsTextView);
        createProfileButton = (Button) findViewById(R.id.createProfileButton);
        enterButton = (Button) findViewById(R.id.enterButton);
        userSpinner = (Spinner) findViewById(R.id.profileSpinner);

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

        /* Setup spinner and adapter */
        List<String> usernames = new ArrayList<String>();
        usernames.add("");
        usernames.add("Adam");
        usernames.add("Beachball");
        usernames.add("tweety83");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, usernames);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(arrayAdapter);
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
        intent.putExtra("username", userSpinner.getSelectedItem().toString());
        startActivity(intent);
    }
}
