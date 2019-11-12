package com.FinalProject.TomAlon.YardenShaish;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.FinalProject.TomAlon.YardenShaish.Fragments.InstallListFragment;
import com.FinalProject.TomAlon.YardenShaish.Fragments.SignatureFragment;
import com.FinalProject.TomAlon.YardenShaish.Install.Install;
import com.FinalProject.TomAlon.YardenShaish.Install.StatusEnum;
import com.FinalProject.TomAlon.YardenShaish.Listeners.UploadImageListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

public class InstallerActivity extends FragmentActivity implements InstallListFragment.OnInstallUpdated, UploadImageListener {
    private static final String TAG = InstallerActivity.class.getSimpleName();

    private static final String INSTALLATIONS_COLLECTION_TAG = "installations";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private InstallListFragment mInstallListFragment;

    private StorageReference mStorageRef;

    private LinearLayout buttonsBar;
    private Button reachedBtn;
    private Button finishedBtn;

    private Install selectedInstall;
    private int currentPosition = -1;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Fade());
        getWindow().setEnterTransition(new Fade());

        setContentView(R.layout.activity_installer);

        startTrackerService();

        mInstallListFragment = new InstallListFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mInstallListFragment).commit();

        buttonsBar = findViewById(R.id.buttons_bar);
        reachedBtn = findViewById(R.id.btn_arrived);
        finishedBtn = findViewById(R.id.btn_finished);

        reachedBtn.setEnabled(false);
        finishedBtn.setEnabled(false);

        reachedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInstallListFragment.updateInstallStatus(StatusEnum.REACHED, currentPosition);
                mInstallListFragment.notifyAdapter();
                sendNotification();
                reachedBtn.setEnabled(false);
                finishedBtn.setEnabled(true);
            }
        });

        finishedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInstallListFragment.updateInstallStatus(StatusEnum.FINISHED, currentPosition);
                mInstallListFragment.notifyAdapter();
                buttonsBar.setVisibility(View.GONE);
                dispatchTakePictureIntent();
            }
        });
    }

    void shiftFragments(boolean signature) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this shiftFragments,
        // and add the transaction to the back stack so the user can navigate back
        Fragment fragment;
        if (signature) {
            buttonsBar.setVisibility(View.GONE);
            fragment = new SignatureFragment();
            reachedBtn.setEnabled(false);
            finishedBtn.setEnabled(false);
        } else {
            buttonsBar.setVisibility(View.VISIBLE);
            fragment = mInstallListFragment = new InstallListFragment();
        }
        transaction.replace(R.id.fragment_container, fragment);

        // Commit the transaction
        transaction.commitAllowingStateLoss();
    }

    void startTrackerService() {
        Intent intent = new Intent(this, TrackerActivity.class);
        startActivity(intent);
    }

    private void sendNotification() {
        Toast.makeText(this, "Manager notified", Toast.LENGTH_SHORT).show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String send_email = "admin@yarden.com";
                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic MTY3NmNjZjYtZmRkNi00MGYwLTkzYmEtZjVlZDdkZjJmNGVh");
                        con.setRequestMethod("POST");
                        String installerMessage = "Your installer arrived to " + selectedInstall.getName();
                        String strJsonBody = "{"
                                + "\"app_id\": \"d618f5fb-dba2-43ad-b1d4-abe1738966b1\","

                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_email + "\"}],"

                                + "\"data\": {\"foo\": \"bar\"},"
                                + "\"contents\": {\"en\": \"" + installerMessage + "\"}"
                                + "}";


                        Log.d(TAG, "strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes(StandardCharsets.UTF_8);
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();
                        Log.d(TAG, "httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        } else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        Log.d(TAG, "jsonResponse:\n" + jsonResponse);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void updateHolderViaParentActivity(InstallListFragment.ContactViewHolder holder, final Install currentInstall, final int position) {
        final StatusEnum status = currentInstall.getStatusEnum();
        int backgroundColor = getResources().getColor(R.color.yarden_light);
        int textColor = Color.BLACK;
        boolean isPressed = currentPosition == position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reachedBtn.setEnabled(true);
                finishedBtn.setEnabled(true);
                currentPosition = position;
                selectedInstall = currentInstall;

                mInstallListFragment.notifyAdapter();

                switch (status) {
                    case WAITING:
                        reachedBtn.setEnabled(true);
                        finishedBtn.setEnabled(false);
                        break;
                    case REACHED:
                        reachedBtn.setEnabled(false);
                        finishedBtn.setEnabled(true);
                        break;
                }
            }
        });
        switch (status) {
            case FINISHED:
                holder.itemView.setClickable(false);
                backgroundColor = Color.GRAY;
                textColor = Color.BLACK;
                break;
            default:
                if (isPressed) {
                    backgroundColor = getResources().getColor(R.color.yarden);
                    textColor = getResources().getColor(R.color.yarden_light);
                }
        }
        holder.setColors(backgroundColor, textColor);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            final Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            uploadImage(bitmap, false);
            shiftFragments(true);
        } else {
            buttonsBar.setVisibility(View.VISIBLE);
            FirebaseFirestore.getInstance()
                    .collection(INSTALLATIONS_COLLECTION_TAG)
                    .document(selectedInstall.getUid())
                    .update("status", StatusEnum.REACHED.toString());

        }
    }

    @Override
    public void uploadImage(Bitmap bitmap, final boolean isSignature) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        // Get the data from an ImageView as bytes
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        final String installID = selectedInstall.getUid();

        mStorageRef = FirebaseStorage.getInstance().getReference().child(installID)
                .child((isSignature ? "signature" : "install") + ".jpg");
        UploadTask uploadTask = mStorageRef.putBytes(data);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                progressDialog.dismiss();
                String toastText;
                if (task.isSuccessful()) {
                    Log.d(TAG, "image uploaded successfully");
                    toastText = "Uploaded";
                    String uploadedImageUrl = task.getResult()
                            .getMetadata()
                            .getReference()
                            .getDownloadUrl()
                            .toString();
                    String field = isSignature ? "signatureImage" : "installImage";
                    Task<Void> installations = FirebaseFirestore.getInstance()
                            .collection(INSTALLATIONS_COLLECTION_TAG)
                            .document(installID)
                            .update(field, uploadedImageUrl);
                } else {
                    Log.d(TAG, "image upload failed");
                    toastText = "Failed " + Objects.requireNonNull(task.getException()).getMessage();
                }
                Toast.makeText(InstallerActivity.this, toastText, Toast.LENGTH_SHORT).show();
                mStorageRef = null;
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                        .getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            }
        });

        if (isSignature) {
            shiftFragments(false);
        }
    }
}
