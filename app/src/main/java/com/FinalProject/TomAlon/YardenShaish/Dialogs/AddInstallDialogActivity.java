package com.FinalProject.TomAlon.YardenShaish.Dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.FinalProject.TomAlon.YardenShaish.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddInstallDialogActivity extends AppCompatActivity {

    private static final String TAG = AddInstallDialogActivity.class.getSimpleName();
    private static final String INSTALLATION_TAG = "installations";
    private FirebaseFirestore mFirestoreDB = FirebaseFirestore.getInstance();

    private EditText nameText;
    private EditText phoneText;
    private EditText addressText;
    private EditText marbleSizeText;
    private EditText dateText;
    private EditText timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_install_dialog);
        nameText = findViewById(R.id.input_name);
        phoneText = findViewById(R.id.input_phone);
        addressText = findViewById(R.id.input_address);
        marbleSizeText = findViewById(R.id.input_marble_size);
        dateText = findViewById(R.id.input_date);
        timeText = findViewById(R.id.input_time);

        Button addBtn = findViewById(R.id.btn_add);
        Button cancelBtn = findViewById(R.id.btn_cancel);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> installMap = createInstallMap();
                uploadToFirestore(installMap);
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private Map<String, Object> createInstallMap() {
        Map<String, Object> install = new HashMap<>();

        String date = dateText.getText().toString();
        String time = timeText.getText().toString();
        Date dateObj = formatDate(date, time);

        install.put("name", nameText.getText().toString());
        install.put("phone", phoneText.getText().toString());
        install.put("address", addressText.getText().toString());
        install.put("marbleSize", marbleSizeText.getText().toString());
        install.put("time", dateObj);
        install.put("status", "Waiting");

        return install;
    }

    private void uploadToFirestore(Map<String, Object> install) {
        mFirestoreDB.collection(INSTALLATION_TAG)
                .add(install)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "install added successfully");
                            String installID = task.getResult().getId();
                            mFirestoreDB.collection(INSTALLATION_TAG)
                                    .document(installID)
                                    .update("uid", installID);
                        } else {
                            Log.w(TAG, task.getException());
                        }
                    }
                });
    }

    private Date formatDate(String date, String time) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy k:mm", Locale.ENGLISH);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        date = date.concat("/" + currentYear)
                .replaceAll("\\s", "");
        time = time.replaceAll("\\s", "");

        Date dateObj = null;
        try {
            dateObj = format.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateObj;
    }
}
