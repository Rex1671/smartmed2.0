package com.example.smartmedbeta;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SessionDetailsActivity extends AppCompatActivity {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(SessionDetailsActivity.class);
    TextView tvDiagnosis, tvConversations, tvFurtherTests, tvMedications;
    TextView tvFullName, tvEmail, tvDOB, tvGender, tvPhone, tvEmergencyContact, tvHeight, tvWeight, tvBMI, tvAge;
public static final String TAG = "SessionDetailsActivity";
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userRef;
    private RecyclerView recyclerViewChat;
    private EditText etUserQuery;
    private Button btnSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private static final String API_KEY = "AIzaSyDTMqspY_C4KUwAqozRE7STorg0-HZD2yU";

    private String dob, email, emergencyContact, gender, height, weight;
    private String prompt1;
    private int i;

///////////////////////////\\
private ProgressBar progressBar2;
    private LinearLayout GMP_LAY;
    private LinearLayout UD_LAY;
    private LinearLayout IDD_LAY;

private LinearLayout patient_data;
private LinearLayout UD_data;
private TextView tvMedicalPlan;
    private ImageView UD_image;
    private ImageView GMP_image;
    private ImageView IDD_image;
    ///////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        GMP_LAY=findViewById(R.id.GMP_LAY);
        UD_LAY=findViewById(R.id.UD_LAY);
        IDD_LAY=findViewById(R.id.IDD_LAY);


        UD_image=findViewById(R.id.UD_image);
        GMP_image=findViewById(R.id.GMP_image);
        IDD_image=findViewById(R.id.IDD_image);
        patient_data=findViewById(R.id.patient_data);
        UD_data=findViewById(R.id.UD_data);
        tvMedicalPlan=findViewById(R.id.tvMedicalPlan);
        progressBar2=findViewById(R.id.progressBar2);
  tvDiagnosis = findViewById(R.id.tvDiagnosisDetails);
        tvConversations = findViewById(R.id.tvConversationsDetails);
        tvFurtherTests = findViewById(R.id.tvFurtherTestsDetails);
        tvMedications = findViewById(R.id.tvMedicationsDetails);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvDOB = findViewById(R.id.tvDOB);
        tvGender = findViewById(R.id.tvGender);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmergencyContact = findViewById(R.id.tvEmergencyContact);
        tvHeight = findViewById(R.id.tvHeight);
        tvWeight = findViewById(R.id.tvWeight);
        tvBMI = findViewById(R.id.tvBMI);
        tvAge = findViewById(R.id.tvAge);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        userRef = firebaseDatabase.getReference("patients").child(userId);

        fetchUserData(null);

        String diagnosis = getIntent().getStringExtra("diagnosis");
        String conversations = getIntent().getStringExtra("conversations");
        String furtherTests = getIntent().getStringExtra("furtherTests");
        String medications = getIntent().getStringExtra("medications");

        tvDiagnosis.setText(diagnosis);
        tvConversations.setText(conversations);
        tvFurtherTests.setText(furtherTests);
        tvMedications.setText(medications);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etUserQuery = findViewById(R.id.etUserQuery);
        btnSend = findViewById(R.id.btnSend);

        chatAdapter = new ChatAdapter(chatMessages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
        sendInitialMessage();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userQuery = etUserQuery.getText().toString().trim();
                if (!userQuery.isEmpty()) {
                    progressBar2.setVisibility(View.VISIBLE);
                    fetchUserData("Send");
                    sendQueryToGemini(userQuery);
                    etUserQuery.setText("");
                } else {
                    Toast.makeText(SessionDetailsActivity.this, "Please enter a query.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void sendInitialMessage() {
        String initialMessage = "Hello! I’m here to help you with your medical concerns. " +
                "Feel free to ask me anything about your health or your medical plan. " +
                "How can I assist you today?";
 chatMessages.add(new ChatMessage("AI", initialMessage));
        chatAdapter.notifyDataSetChanged();
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

    }
 private void sendQueryToGemini(String userQuery) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("User Data:\n")
                .append("DOB: ").append(dob).append("\n")
                .append("Gender: ").append(gender).append("\n")
                .append("Height: ").append(height).append(" cm\n")
                .append("Weight: ").append(weight).append(" kg\n")
                .append("Previous Conversation:\n");
        for (ChatMessage message : chatMessages) {
            promptBuilder.append(message.getRole()).append(": ").append(message.getText()).append("\n");
        }
        promptBuilder.append("User: ").append(userQuery);
        String prompt = promptBuilder.toString();
        askGemini(prompt);

    }
    private void askGemini(String prompt) {

       //////////////////////////////////////
        prompt = createPrompt(prompt);
        Log.d(TAG, "Prompt sent to Gemini: " + prompt);
        prompt1=prompt;
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder().addText(prompt).build();
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                Log.d(TAG, "API Response: " + resultText);
                onGeminiResponse(resultText);
//                processApiResponse(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
//                loadingProgressBar.setVisibility(View.GONE);
                Log.d(TAG, "Calling Gemini again for "+i+" times");
                ++i;
                askGemini(prompt1);
                runOnUiThread(() -> Toast.makeText(SessionDetailsActivity.this, "Error communicating with the server", Toast.LENGTH_SHORT).show());
            }
        }, executor);



        ////////////////////////////////////////////


    }
    private String createPrompt(String userQuery) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("Objective: Create a comprehensive and customized medical plan for the user that focuses on recovery and overall well-being. The user can modify this plan and ask questions regarding it.\n\n");
        promptBuilder.append("User Data:\n")
                .append("Full Name: ").append(tvFullName.getText()).append("\n")
                .append("DOB: ").append(dob).append("\n")
                .append("Gender: ").append(gender).append("\n")
                .append("Height: ").append(tvHeight.getText()).append(" cm\n")
                .append("Weight: ").append(tvWeight.getText()).append(" kg\n")
                .append("Email: ").append(tvEmail.getText()).append("\n")
                .append("Phone: ").append(tvPhone.getText()).append("\n")
                .append("Emergency Contact: ").append(tvEmergencyContact.getText()).append("\n")
                .append("Age: ").append(tvAge.getText()).append("\n")
                .append("BMI: ").append(tvBMI.getText()).append("\n\n");

        promptBuilder.append("Current Health Conditions:\n")
                .append(tvDiagnosis.getText()).append("\n\n");

        promptBuilder.append("Current Medications:\n")
                .append(tvMedications.getText()).append("\n\n");

        promptBuilder.append("Previous Conversations:\n");
        for (ChatMessage message : chatMessages) {
            promptBuilder.append(message.getRole()).append(": ").append(message.getText()).append("\n");
        }


        promptBuilder.append("User Query: ").append(userQuery).append("\n\n");

        promptBuilder.append("Instructions for AI: Based on the user data and their query, generate a comprehensive and customized medical plan focusing on recovery and overall well-being in JSON format. Use the following structure:\n")
                .append("{\n")
                .append("  \"medical_plan\": {\n")
                .append("    \"full_name\": \"[Full Name]\",\n")
                .append("    \"dietary_suggestions\": \"[Include detailed dietary suggestions, considering any specific restrictions or preferences]\",\n")
                .append("    \"exercise_recommendations\": \"[Include specific types of exercises, durations, and frequency tailored to the user's condition]\",\n")
                .append("    \"mental_health_strategies\": \"[Include strategies for managing stress and promoting mental well-being]\",\n")
                .append("    \"sleep_hygiene_tips\": \"[Include recommendations for improving sleep quality]\",\n")
                .append("    \"further_tests\": \"[Include any recommended tests or follow-ups to monitor progress]\",\n")
                .append("    \"lifestyle_changes\": \"[Suggest practical lifestyle changes to support recovery, like hydration, avoiding alcohol, etc.]\",\n")
                .append("    \"follow_up_recommendations\": \"[Specify any follow-up appointments or actions needed to monitor health progress]\"\n")
                .append("  },\n")
                .append("  \"user_response\": \"[Response to User Query]\"\n")
                .append("}\n")
                .append("\nEnsure the JSON response is valid and well-structured, and consider any additional factors that may impact the user's recovery and overall health.");

        return promptBuilder.toString();
    }

    private void onGeminiResponse(String response) {
       response = response.trim();

        if (response.startsWith("```json")) {

            response = response.substring("```json".length()).trim();
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - "```".length()).trim();
        }
        if (!isJsonValid(response)) {
            Log.e(TAG, "Invalid JSON response: " + response);
            runOnUiThread(() -> {
                Toast.makeText(this, "Error processing response. Please try again.", Toast.LENGTH_SHORT).show();
            });
            Log.d(TAG, "Calling Gemini again for "+i+" times");
            ++i;
            askGemini(prompt1);
            return;
        }

        try {
            JSONObject jsonResponse = new JSONObject(response);

            if (!jsonResponse.has("medical_plan")) {
                Log.e(TAG, "Response does not contain medical_plan");
                runOnUiThread(() -> {
                    Toast.makeText(this, "Invalid response format. Please try again.", Toast.LENGTH_SHORT).show();
                });
                Log.d(TAG, "Calling Gemini again for "+i+" times");
                ++i;
                askGemini(prompt1);
                return;
            }
            JSONObject medicalPlan = jsonResponse.getJSONObject("medical_plan");

            String fullName = medicalPlan.optString("full_name", "N/A");
            String dietarySuggestions = medicalPlan.optString("dietary_suggestions", "N/A");
            String exerciseRecommendations = medicalPlan.optString("exercise_recommendations", "N/A");
            String mentalHealthStrategies = medicalPlan.optString("mental_health_strategies", "N/A");
            String sleepHygieneTips = medicalPlan.optString("sleep_hygiene_tips", "N/A");
            String furtherTests = medicalPlan.optString("further_tests", "N/A");
            String lifestyleChanges = medicalPlan.optString("lifestyle_changes", "N/A");
            String followUpRecommendations = medicalPlan.optString("follow_up_recommendations", "N/A");

            SpannableStringBuilder formattedMedicalPlan = createStyledMedicalPlan(
                    fullName,
                    dietarySuggestions,
                    exerciseRecommendations,
                    mentalHealthStrategies,
                    sleepHygieneTips,
                    furtherTests,
                    lifestyleChanges,
                    followUpRecommendations
            );


            runOnUiThread(() -> {
                GMP_LAY.setVisibility(View.VISIBLE);
                progressBar2.setVisibility(View.GONE);
LinearLayout medicalplan=findViewById(R.id.medicalplan);
                TextView tvMedicalPlan = findViewById(R.id.tvMedicalPlan);
                medicalplan.setVisibility(View.VISIBLE);
                tvMedicalPlan.setText(formattedMedicalPlan);

                String userResponse = jsonResponse.optString("user_response", "N/A");

                chatMessages.add(new ChatMessage("AI", userResponse));
                chatAdapter.notifyDataSetChanged();
                recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
            });


        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage());
            runOnUiThread(() -> {
                Toast.makeText(this, "Error processing response. Please try again.", Toast.LENGTH_SHORT).show();
            });
        }
    }
 private SpannableStringBuilder createStyledMedicalPlan(
            String fullName,
            String dietarySuggestions,
            String exerciseRecommendations,
            String mentalHealthStrategies,
            String sleepHygieneTips,
            String furtherTests,
            String lifestyleChanges,
            String followUpRecommendations) {

        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

        appendStyledText(spannableBuilder, "Medical Plan for ", fullName);
        appendStyledText(spannableBuilder, "Dietary Suggestions: ", dietarySuggestions);
        appendStyledText(spannableBuilder, "Exercise Recommendations: ", exerciseRecommendations);
        appendStyledText(spannableBuilder, "Mental Health Strategies: ", mentalHealthStrategies);
        appendStyledText(spannableBuilder, "Sleep Hygiene Tips: ", sleepHygieneTips);
        appendStyledText(spannableBuilder, "Further Tests: ", furtherTests);
        appendStyledText(spannableBuilder, "Lifestyle Changes: ", lifestyleChanges);
        appendStyledText(spannableBuilder, "Follow-up Recommendations: ", followUpRecommendations);

        return spannableBuilder;
    }

    private void appendStyledText(SpannableStringBuilder spannableBuilder, String label, String value) {
        int start = spannableBuilder.length();
        spannableBuilder.append(label);
        int end = spannableBuilder.length();
spannableBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.setSpan(new RelativeSizeSpan(1.2f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableBuilder.append(value).append("\n\n");
    }


    private boolean isJsonValid(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException ex) {
            try {
                new JSONArray(json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
    private String getSafeString(DataSnapshot snapshot) {
        String value = snapshot.getValue(String.class);
        return value != null ? value : "N/A";
    }

    private void fetchUserData(String send) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullName = getSafeString(dataSnapshot.child("userdata").child("fullName"));
                    String email = getSafeString(dataSnapshot.child("userdata").child("email"));
                    dob = getSafeString(dataSnapshot.child("userdata").child("dob"));
                    gender = getSafeString(dataSnapshot.child("userdata").child("gender"));
                    String phone = getSafeString(dataSnapshot.child("userdata").child("phone"));
                    String emergencyContact = getSafeString(dataSnapshot.child("userdata").child("emergencyContact"));
                    String heightStr = getSafeString(dataSnapshot.child("userdata").child("height"));
                    String weightStr = getSafeString(dataSnapshot.child("userdata").child("weight"));

                    tvFullName.setText(createStyledText("Full Name: ", fullName));
                    tvEmail.setText(createStyledText("Email: ", email));
                    tvDOB.setText(createStyledText("Date of Birth: ", dob));
                    tvGender.setText(createStyledText("Gender: ", gender));
                    tvPhone.setText(createStyledText("Phone: ", phone));
                    tvEmergencyContact.setText(createStyledText("Emergency Contact: ", emergencyContact));
                    tvHeight.setText(createStyledText("Height (cm): ", heightStr));
                    tvWeight.setText(createStyledText("Weight (kg): ", weightStr));

                    if (!dob.equals("N/A")) {
                        int age = calculateAge(dob);
                        tvAge.setText(createStyledText("Age: ", String.valueOf(age)));
                    }

                    if (!heightStr.equals("N/A") && !weightStr.equals("N/A")) {
                        double bmi = calculateBMI(Double.parseDouble(heightStr), Double.parseDouble(weightStr));
                        tvBMI.setText(createStyledText("BMI: ", String.format("%.2f", bmi)));
                    }

                    String userQuery = etUserQuery.getText().toString();
                    if (send != null && !userQuery.isEmpty()) {
                        sendQueryToGemini(userQuery);
                    }
                } else {
                    Log.e("SessionDetailsActivity", "User data not found");
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SessionDetailsActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

 private SpannableStringBuilder createStyledText(String hardcodedText, String dynamicText) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

        int start = spannableBuilder.length();
        spannableBuilder.append(hardcodedText);
        int end = spannableBuilder.length();
        spannableBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Change color
        spannableBuilder.setSpan(new RelativeSizeSpan(1.2f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Change size

        spannableBuilder.append(dynamicText);

        return spannableBuilder;
    }
 private int calculateAge(String dob) {
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
        try {
            Date birthDate = sdf.parse(dob);
            Calendar birthCalendar = Calendar.getInstance();
            birthCalendar.setTime(birthDate);
            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (ParseException e) {
            Log.e("SessionDetailsActivity", "Error parsing date: " + e.getMessage());
            return 0;
        }
    }

    private double calculateBMI(double height, double weight) {
        height = height / 100;
        return weight / (height * height);
    }
}
