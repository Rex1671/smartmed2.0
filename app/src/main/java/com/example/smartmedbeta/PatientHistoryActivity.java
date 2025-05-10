package com.example.smartmedbeta;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatientHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference patientRef;
    private String patientId;
    private PatientHistoryAdapter adapter;
    private List<SessionData> sessionDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_history);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#F4F4F4"));
        }
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        patientId = mAuth.getCurrentUser().getUid();
        patientRef = firebaseDatabase.getReference("patients").child(patientId).child("sessions");


        fetchPatientHistoryData();
    }

    private void fetchPatientHistoryData() {
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sessionDataList.clear();
                for (DataSnapshot sessionSnapshot : dataSnapshot.getChildren()) {
                    String sessionId = sessionSnapshot.getKey();
                    SessionData sessionData = new SessionData(sessionId);

                    for (DataSnapshot detailSnapshot : sessionSnapshot.getChildren()) {
                        switch (detailSnapshot.getKey()) {
                            case "conversations":
                                handleConversations(detailSnapshot, sessionData);
                                break;
                            case "diagnosis":
                                handleDiagnosis(detailSnapshot, sessionData);
                                break;
                            case "furtherTests":
                                handleFurtherTests(detailSnapshot, sessionData);
                                break;
                            case "medications":
                                handleMedications(detailSnapshot, sessionData);
                                break;
                        }
                    }
                    sessionDataList.add(sessionData);
                }

                adapter = new PatientHistoryAdapter(sessionDataList, PatientHistoryActivity.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PatientHistoryActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void handleConversations(DataSnapshot detailSnapshot, SessionData sessionData) {
        Map<String, Map<String, String>> conversations =
                (Map<String, Map<String, String>>) detailSnapshot.getValue();
        sessionData.setConversations(conversations);
    }

    private void handleDiagnosis(DataSnapshot detailSnapshot, SessionData sessionData) {
        Diagnosis diagnosis = detailSnapshot.getValue(Diagnosis.class);
        sessionData.setDiagnosis(diagnosis);
    }

    private void handleFurtherTests(DataSnapshot detailSnapshot, SessionData sessionData) {
        Object furtherTestsData = detailSnapshot.getValue();
        List<String> furtherTestsList = new ArrayList<>();

        if (furtherTestsData instanceof List) {
            List<?> listData = (List<?>) furtherTestsData;
            for (Object test : listData) {
                if (test instanceof String) {
                    furtherTestsList.add((String) test);
                } else {
                    Log.e("PatientHistoryActivity", "Non-string data found in furtherTestsList: " + test.getClass().getName());
                }
            }
        } else if (furtherTestsData instanceof Map) {
            furtherTestsList.addAll(((Map<String, String>) furtherTestsData).values());
        } else {
            Log.e("PatientHistoryActivity", "Unexpected data type for furtherTestsData: " + furtherTestsData.getClass().getName());
        }

        sessionData.setFurtherTestsDetails(furtherTestsList);
    }

    private void handleMedications(DataSnapshot detailSnapshot, SessionData sessionData) {
        Object medicationsData = detailSnapshot.getValue();
        List<Map<String, Object>> medicationsList = new ArrayList<>();

        if (medicationsData instanceof List) {
            medicationsList = (List<Map<String, Object>>) medicationsData;
        } else if (medicationsData instanceof Map) {
            medicationsList = new ArrayList<>(((Map<String, Map<String, Object>>) medicationsData).values());
        }

        sessionData.setMedications(medicationsList);
        StringBuilder medicationsDetails = new StringBuilder("Medications Details:\n");
        for (Map<String, Object> medication : medicationsList) {
            String name = (String) medication.get("name");
            String duration = (String) medication.get("duration");
            String dose = (String) medication.get("dose");
            List<String> sideEffects = (List<String>) medication.get("sideEffects");

            medicationsDetails.append("- Name: ").append(name).append("\n");
            medicationsDetails.append("  Duration: ").append(duration).append("\n");
            medicationsDetails.append("  Dose: ").append(dose).append("\n");
            medicationsDetails.append("  Side Effects: ").append(String.join(", ", sideEffects)).append("\n");
            medicationsDetails.append("\n");
        }
        sessionData.setMedicationsDetails(medicationsDetails.toString());
    }




}
