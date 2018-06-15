package edu.tamu.adamhair.apraxiaworldrecorder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.database.User;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.UserViewModel;

public class SplashScreen extends AppCompatActivity {

    private UserViewModel userViewModel;
    List<Integer> userIds;

    /* Firebase authentication variables */
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    /* UI elements */
    TextView instructionsTextView;
    Button createProfileButton;
    Button enterButton;
    Spinner userSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        /* Set up authentication and callbacks */
        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("FirebaseAuth", user.getUid());
                } else {
                    Log.d("FirebaseAuth", "User logged out");
                }
            }
        };
        signInAnonymously();

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
            public void onClick(View view) {switchToWordSelectionActivity(view);
            }
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

        userViewModel.getAllUsersSorted().observe(SplashScreen.this, new Observer<List<User>>() {
            @Override
            public void onChanged(@Nullable List<User> users) {
                for (int i = 0; i < users.size(); i++) {
                    arrayAdapter.add(users.get(i).getUsername());
                    userIds.add(users.get(i).getUid());
                }

            }
        });

        /* Get file system permissions */
        if (!FileManager.checkPermissions(this)) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("We need some app permissions")
                    .setMessage("To record and save audio, we need access to your device microphone and files." +
                            " We don't look at any files that this app didn't create." +
                            " Please grant these permissions to continue using this app.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FileManager.checkAndRequestPermissions(SplashScreen.this);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    private void switchToInstructionActivity(View view) {
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }

    private void switchToCreateProfileActivity(View view) {
        if (FileManager.checkAndRequestPermissions(this)) {
            /* Make Apraxia World folder, if necessary */
            if (!FileManager.awFolderExists()) {
                Log.d("Splash screen", "Making AW folder");
                FileManager.createAwFolder();
            }

            Intent intent = new Intent(this, ProfileCreatorActivity.class);
            startActivity(intent);
        }
    }

    private void signInAnonymously() {
        auth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.i("FirebaseAuth", "Sign in onComplete: " + task.isSuccessful());
            }
        });
    }

    private void switchToWordSelectionActivity(View view) {
        if (FileManager.checkAndRequestPermissions(this)) {
            Intent intent = new Intent(this, WordSelectionActivity.class);
            intent.putExtra("username", userSpinner.getSelectedItem().toString());
            intent.putExtra("userId", userIds.get(userSpinner.getSelectedItemPosition()-1));
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        // Go to phone home, don't go back to another activity
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
