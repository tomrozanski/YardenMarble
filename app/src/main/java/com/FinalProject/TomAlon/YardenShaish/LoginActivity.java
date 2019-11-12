package com.FinalProject.TomAlon.YardenShaish;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.onesignal.OneSignal;

import java.util.Objects;

//Please read the Readme.txt for instructions!
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final String USERS_TAG = "users";
    private static final String IS_ADMIN_TAG = "isAdmin";

    // widgets
    private EditText mEmailText;
    private EditText mPasswordText;
    private Button mLoginButton;

    // dialogs
    private ProgressDialog mProgressDialog;

    // firebase
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Fade());
        getWindow().setEnterTransition(new Fade());


        setContentView(R.layout.activity_login);


        // dialog initialization
        mProgressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);

        // widgets initialization
        mEmailText = findViewById(R.id.input_user);
        mPasswordText = findViewById(R.id.input_password);
        mLoginButton = findViewById(R.id.btn_login);

        // listeners
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login() {
        Log.d(TAG, "login button pressed");
        // validate if the username / password is in the right format
        if (!validate()) {
            onLoginFailure();
            return;
        }
        mLoginButton.setEnabled(false);

        showProgressDialog();

        String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "successful login");
                            onLoginSuccess();
                        } else {
                            Log.d(TAG, task.getException().toString());
                            onLoginFailure();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void showProgressDialog() {
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Authenticating...");
        mProgressDialog.show();
    }

    public void onLoginSuccess() {
        mProgressDialog.dismiss();
        nextActivity();
    }

    public void onLoginFailure() {
        mProgressDialog.dismiss();
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        mLoginButton.setEnabled(true);
    }

    private void nextActivity() {
        Log.d(TAG, "user connected");
        // OneSignal: set device for push notification
        FirebaseUser user = mAuth.getCurrentUser();
        String LoggedIn_User_Email = user.getEmail();
        OneSignal.sendTag("User_ID", LoggedIn_User_Email);

        // the user's ID that collected from the firestore authentication
        // is the same as the corresponding user document's ID in the firestore database
        String userID = user.getUid();
        FirebaseFirestore mFireStoreDB = FirebaseFirestore.getInstance();
        mFireStoreDB.collection(USERS_TAG)
                .document(userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean isAdmin = task.getResult().getBoolean(IS_ADMIN_TAG);
                            Intent intent = new Intent(getApplicationContext(), isAdmin ? AdminActivity.class : InstallerActivity.class);
                            startActivity(intent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this).toBundle() : null);
                            finish();
                        } else {
                            Log.d(TAG, Objects.requireNonNull(task.getException()).toString());
                        }
                    }
                });
    }

    public boolean validate() {
        boolean valid = true;

        String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailText.setError("enter a valid email address");
            valid = false;
        } else {
            mEmailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        return valid;
    }
}