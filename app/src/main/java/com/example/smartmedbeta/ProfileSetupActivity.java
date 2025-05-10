package com.example.smartmedbeta;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etFullName, etEmail, etPhone, etDOB, etHeight, etWeight, etEmergencyContact;
    private Spinner spinnerGender;
    private CheckBox cbTerms;
    private Button btnSubmit;
    private TextView btnUploadPicture;
    private ImageView profileImageView;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures");
        loadUserProfileData();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etDOB = findViewById(R.id.etDOB);
        spinnerGender = findViewById(R.id.spinnerGender);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        cbTerms = findViewById(R.id.cbTerms);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUploadPicture = findViewById(R.id.btnUploadPicture);
        profileImageView = findViewById(R.id.profileImageView);
        progressBar = findViewById(R.id.progressBar);

        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spinnerGender.setAdapter(adapter);

        btnUploadPicture.setOnClickListener(v -> openFileChooser());

        btnSubmit.setOnClickListener(v -> submitProfile());


        etDOB.setOnClickListener(v -> showDatePickerDialog());
    }
    private void showDatePickerDialog() {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    etDOB.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
                }, year, month, day);

        datePickerDialog.show();
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }
    private void loadUserProfileData() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = databaseReference.child("patients").child(userId).child("userdata");

          userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                      Map<String, Object> userData = (Map<String, Object>) task.getResult().getValue();

                     if (userData.get("fullName") != null) {
                        etFullName.setText(userData.get("fullName").toString());
                    }
                    if (userData.get("email") != null) {
                        etEmail.setText(userData.get("email").toString());
                    }
                    if (userData.get("phone") != null) {
                        etPhone.setText(userData.get("phone").toString());
                    }
                    if (userData.get("dob") != null) {
                        etDOB.setText(userData.get("dob").toString());
                    }
                    if (userData.get("gender") != null) {
                        String gender = userData.get("gender").toString();
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerGender.getAdapter();
                        int position = adapter.getPosition(gender);
                        spinnerGender.setSelection(position);
                    }
                    if (userData.get("height") != null) {
                        etHeight.setText(userData.get("height").toString());
                    }
                    if (userData.get("weight") != null) {
                        etWeight.setText(userData.get("weight").toString());
                    }
                    if (userData.get("emergencyContact") != null) {
                        etEmergencyContact.setText(userData.get("emergencyContact").toString());
                    }
                    if (userData.get("profileImage") != null) {
                        String imageUrl = userData.get("profileImage").toString();
                          Glide.with(this).load(imageUrl).into(profileImageView);
                        saveImageUrlLocally(imageUrl);
                    }
                }
            } else {
                Toast.makeText(ProfileSetupActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void saveImageUrlLocally(String imageUrl) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profileImageUrl", imageUrl);
        editor.apply();
    }


    private void submitProfile() {

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String height = etHeight.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String emergencyContact = etEmergencyContact.getText().toString().trim();
        boolean termsAccepted = cbTerms.isChecked();


        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || dob.isEmpty() || gender.equals("Select Gender") ||
                height.isEmpty() || weight.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!termsAccepted) {
            Toast.makeText(this, "You must accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }


        progressBar.setVisibility(View.VISIBLE);

        String userId = mAuth.getCurrentUser().getUid();


        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(userId + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {

                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            storeUserData(userId, fullName, email, phone, dob, gender, height, weight, emergencyContact, imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {

                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileSetupActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {

            storeUserData(userId, fullName, email, phone, dob, gender, height, weight, emergencyContact, null);
        }
    }

    private void storeUserData(String userId, String fullName, String email, String phone, String dob, String gender,
                               String height, String weight, String emergencyContact, String imageUrl) {

        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("dob", dob);
        userData.put("gender", gender);
        userData.put("height", height);
        userData.put("weight", weight);
        userData.put("emergencyContact", emergencyContact);
        if (imageUrl != null) {
            userData.put("profileImage", imageUrl);
        }

        databaseReference.child("patients").child(userId).child("userdata").setValue(userData)
                .addOnCompleteListener(task -> {

                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileSetupActivity.this, "Profile setup complete!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProfileSetupActivity.this, MainActivity.class));
                    } else {
                        Toast.makeText(ProfileSetupActivity.this, "Failed to save profile data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
