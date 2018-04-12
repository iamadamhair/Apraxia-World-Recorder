package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.User;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RepetitionViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.UserViewModel;

public class ProfileCreatorActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private RepetitionViewModel repetitionViewModel;
    private List<String> usernames;
    private boolean usernamesLoaded;

    /* UI Elements */
    Button createButton;
    TextView infoTextView;
    EditText usernameEditText;
    EditText ageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_creator);

        setTitle("Create Apraxia World profile");
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        repetitionViewModel = ViewModelProviders.of(this).get(RepetitionViewModel.class);

        /* Link UI elements */
        createButton = (Button) findViewById(R.id.createButton);
        infoTextView = (TextView) findViewById(R.id.infoTextView);
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        ageEditText = (EditText) findViewById(R.id.ageEditText);

        createButton.setEnabled(false);

        /* Set EditText parameters */
        int maxAgeLength = 2; // Limit age to 99 for ease of validation
        int maxUsernameLength = 16;
        usernameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxUsernameLength)});
        ageEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxAgeLength)});

        infoTextView.setText("Username may only contain letters and numbers");

        /* Add listeners */
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {switchToWordSelectionActivity(view);};
        });
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (validateUsername()) {

                    if (usernameEditText.getText().toString().length() > 0) {
                        infoTextView.setText("Username OK");
                    } else {
                        infoTextView.setText("Username may only contain letters and numbers");
                    }

                    if (areFieldsFilledIn()) {
                        createButton.setEnabled(true);
                    } else {
                        createButton.setEnabled(false);
                    }
                } else {
                    infoTextView.setText("Username already exists\nPlease pick another");
                    createButton.setEnabled(false);
                }
            }
        });
        ageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (validateUsername()) {
                    if (areFieldsFilledIn()) {
                        createButton.setEnabled(true);
                    } else {
                        createButton.setEnabled(false);
                    }
                } else {
                    createButton.setEnabled(false);
                }
            }
        });

        /* Set up list of existing usernames so we can't duplicate */
        usernamesLoaded = false;
        usernames = new ArrayList<>();
        userViewModel.getUsernames().observe(ProfileCreatorActivity.this, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> strings) {
                for (int i = 0; i < strings.size(); i++) {
                    usernames.add(strings.get(i));
                }
                usernamesLoaded = true;
            }
        });
    }


    private void newUserSetup(User user, Intent intent) {
        // Puts the user in the db and populates the repetitions
        usernames.add(user.getUsername());
        new writeUserDatAsync(this).execute(usernames);
        repetitionViewModel.populateRepetitions(this.getResources().getStringArray(R.array.ndp3_images),
                user, intent, getApplication());

    }

    private void switchToWordSelectionActivity(View view) {
        User newUser = new User(usernameEditText.getText().toString(),
                Integer.valueOf(ageEditText.getText().toString()));
        Intent intent = new Intent(this, WordSelectionActivity.class);
        intent.putExtra("username", usernameEditText.getText().toString());

        createButton.setClickable(false);
        infoTextView.setText("Preparing new profile");
        newUserSetup(newUser, intent);
    }

    private boolean validateUsername() {
        if (!usernamesLoaded) {
            // Usernames haven't loaded yet, put up a message to try again in a sec
            infoTextView.setText("Existing usernames still loading, try again");
            return false;
        } else {
            String potentialUsername = usernameEditText.getText().toString();

            for (int i = 0; i < usernames.size(); i++) {
                if (usernames.get(i).equals(potentialUsername)) {
                    // If the username already exists, don't let them add it again
                    return false;
                }
            }
            // Username doesn't exist, go ahead and add it
            return true;
        }
    }

    private boolean areFieldsFilledIn() {
        if (usernameEditText.getText().toString().length() > 0 && ageEditText.getText().toString().length() > 0) {
            return true;
        } else {
            return false;
        }
    }
    private class writeUserDatAsync extends AsyncTask<List<String>, Void, Void> {
        Context mContext;

        writeUserDatAsync(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(final List<String>... params) {
            FileManager.recreateUserDatFile(usernames, mContext);
            return null;
        }
    }

}
