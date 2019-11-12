package com.FinalProject.TomAlon.YardenShaish;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.Window;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.onesignal.OneSignal;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private static final String USERS_TAG = "users";
    private static final String IS_ADMIN_TAG = "isAdmin";
    private static final String USER_ID_TAG = "User_ID";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Fade());
        getWindow().setEnterTransition(new Fade());

        setContentView(R.layout.activity_splash);

        // OneSignal Initialization
        OneSignal.startInit(this).init();

        if (isUserConnected()) {
            connected();
        } else {
            disconnected();
        }
    }

    // Check if we're running on Android 5.0 or higher
    private boolean isGreaterThanLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public boolean isUserConnected() {
        return mAuth.getCurrentUser() != null;
    }

    private void disconnected() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent, isGreaterThanLollipop() ? ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this).toBundle() : null);
        finish();
    }

    private void connected() {
        Log.d(TAG, "user connected");
        // OneSignal: set device for push notification
        FirebaseUser user = mAuth.getCurrentUser();
        String LoggedIn_User_Email = user.getEmail();
        OneSignal.sendTag(USER_ID_TAG, LoggedIn_User_Email);

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
                            startActivity(intent, isGreaterThanLollipop() ? ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this).toBundle() : null);
                            finish();
                        } else {
                            Log.d(TAG, Objects.requireNonNull(task.getException()).toString());
                            disconnected();
                        }
                    }
                });
    }
}
