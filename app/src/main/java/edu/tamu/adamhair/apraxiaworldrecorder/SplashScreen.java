package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.User;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.UserViewModel;

public class SplashScreen extends AppCompatActivity {

    private UserViewModel userViewModel;
    List<Integer> userIds;

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
        userIds = new ArrayList<>();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, usernames);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(arrayAdapter);

        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    enterButton.setEnabled(true);
                } else {
                    enterButton.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {            }
        });

        /* Setup LiveData for usernames */
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        userViewModel.getAllUsers().observe(SplashScreen.this, new Observer<List<User>>() {
            @Override
            public void onChanged(@Nullable List<User> users) {
                for (int i = 0; i < users.size(); i++) {
                    arrayAdapter.add(users.get(i).getUsername());
                    userIds.add(users.get(i).getUid());
                }

            }
        });

        /* Get file system permissions */
        FileManager.checkAndRequestPermissions(this);

        /* Make Apraxia World folder, if necessary */
        if (!FileManager.awFolderExists()) {
            Log.d("Splash screen", "Making AW folder");
            FileManager.createAwFolder();
        }
    }

    private void switchToInstructionActivity(View view) {
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }

    private void switchToCreateProfileActivity(View view) {
        Intent intent = new Intent(this, ProfileCreatorActivity.class);
        startActivity(intent);
    }

    private void switchToWordSelectionActivity(View view) {
        Intent intent = new Intent(this, WordSelectionActivity.class);
        intent.putExtra("username", userSpinner.getSelectedItem().toString());
        intent.putExtra("userId", userIds.get(userSpinner.getSelectedItemPosition()-1));
        startActivity(intent);
    }
}
