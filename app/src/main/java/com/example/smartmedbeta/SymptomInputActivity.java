package com.example.smartmedbeta;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class SymptomInputActivity extends AppCompatActivity {
    private boolean isMultiAIMode = false;

    private static final String TAG = "SymptomInputActivity";
    private String selectedLanguageCode = "en";
    private EditText symptomsEditText;
    private ApiResponse response;
    private TextView outputTextView;
    private Button submitButton ;
    private ImageView selectedImageView,selectImageButton;
    private Uri selectedImageUri;
    private ProgressBar loadingProgressBar;
    private static final int IMAGE_PICK_REQUEST = 1;
    private String previousConversation;
    private static final String GEMINI_API_URL = "https://api.openai.com/v1/images/generations";
    private static final String API_KEY = "AIzaSyCWr7fyy5voqODSyr6h3rhbGT4pWN-IdHI";
    private String currentSymptoms;
    private SpeechRecognizer speechRecognizer;
    private LinearLayout followUpContainer;
    private Button showFollowUpButton;
    private String sessionId;
    private  static String finalprompt1;
    private boolean isPrescriptionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_input);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        symptomsEditText = findViewById(R.id.symptomsInput);
        outputTextView = findViewById(R.id.outputTextView);
        submitButton = findViewById(R.id.submitButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        followUpContainer=findViewById(R.id.followUpContainer);
        selectedImageView = findViewById(R.id.selectedImageView);
        selectImageButton = findViewById(R.id.choose_image);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        ImageView voiceInputButton = findViewById(R.id.voiceInputButton);
        voiceInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        selectImageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isPrescriptionMode = true;
                openGalleryForImage();
                return true;
            }
        });
      showFollowUpButton = findViewById(R.id.showFollowUpButton);
        Button submitFollowUpButton1 = findViewById(R.id.submitFollowUpButton);
showFollowUpButton.setOnClickListener(new View.OnClickListener()
{
    @Override
    public void onClick(View v) {
        followUpContainer.setVisibility(View.VISIBLE);
        showFollowUpButton.setVisibility(View.GONE);
        submitFollowUpButton1.setVisibility(View.VISIBLE);
    }
});



        initializeSession();

        Switch aiModeSwitch = findViewById(R.id.aiModeSwitch);
        aiModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isMultiAIMode = isChecked;
        });



        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSymptoms = symptomsEditText.getText().toString();
                previousConversation = "Patient: \"" + currentSymptoms + "\"\n";

                Log.d(TAG, "Submitting Symptoms: " + currentSymptoms);
                loadingProgressBar.setVisibility(View.VISIBLE);

                if (selectedImageUri != null) {
                    sendRequestToGeminiWithImage(currentSymptoms, selectedImageUri);
                } else {
                    if (isMultiAIMode) {
                        sendParallelAIRequests(currentSymptoms);
                    } else {
                        sendRequestToGemini(currentSymptoms,null);
                    }
                    }
            }
        });
    }

    private void openGalleryForImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (isPrescriptionMode) {

                isPrescriptionMode = false;
                 sendRequestToGeminiWithPrescription(imageUri);
            } else {
                 selectedImageUri = imageUri;
                selectedImageView.setImageURI(selectedImageUri);
            }
        }
    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
//            selectedImageUri = data.getData();
//            selectedImageView.setImageURI(selectedImageUri);
//        }
//    }
    private DiagnosisResult selectBestDiagnosis(List<DiagnosisResult> responses) {
        Map<String, Integer> diagnosisCount = new HashMap<>();
        Map<String, Double> confidenceScores = new HashMap<>();

        for (DiagnosisResult result : responses) {
            if (result.condition.equals("Unknown")) continue;

            diagnosisCount.put(result.condition, diagnosisCount.getOrDefault(result.condition, 0) + 1);
            confidenceScores.put(result.condition, confidenceScores.getOrDefault(result.condition, 0.0) + result.confidence);
        }

        if (diagnosisCount.isEmpty()) return new DiagnosisResult("No valid diagnosis", 0.0);

       return diagnosisCount.entrySet().stream()
                .max(Comparator.comparingDouble(entry -> confidenceScores.get(entry.getKey()) / entry.getValue()))
                .map(entry -> new DiagnosisResult(entry.getKey(), confidenceScores.get(entry.getKey()) / entry.getValue()))
                .orElse(new DiagnosisResult("Unknown", 0.0));
    }



    private void sendParallelAIRequests(String symptoms) {
        Log.d("SymptomInput", "Starting parallel AI requests for symptoms: " + symptoms);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CompletionService<DiagnosisResult> completionService = new ExecutorCompletionService<>(executor);

        List<Callable<DiagnosisResult>> tasks = Arrays.asList(
                () -> queryGeminiAI(symptoms),
                () -> queryHuggingFaceAI(symptoms),
                () -> queryMistralAI(symptoms),
                () -> queryLlama2AI(symptoms)
        );


        for (Callable<DiagnosisResult> task : tasks) {
            completionService.submit(task);
        }

        List<DiagnosisResult> responses = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            try {
                DiagnosisResult result = completionService.take().get(5, TimeUnit.SECONDS); // Timeout per request
                Log.d("SymptomInput", "Received response: " + result);
                responses.add(result);
            } catch (Exception e) {
                Log.d("SymptomInput", "AI request failed: " + e.getMessage());
            }
        }

        executor.shutdown();
        DiagnosisResult bestDiagnosis = selectBestDiagnosis(responses);
        Log.d("SymptomInput", "Best diagnosis selected: " + bestDiagnosis);
        updateOutput(bestDiagnosis);
    }


    private DiagnosisResult queryGeminiAI(String prompt) {
        try {
            Log.d("SymptomInput","Querying Gemini AI with prompt: " + prompt);
            GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY);
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);
            Content content = new Content.Builder().addText(prompt).build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            String resultText = response.get().getText();
            Log.d("SymptomInput","Gemini AI response: " + resultText);

            return new DiagnosisResult(parseDiagnosis(resultText), 0.85);
        } catch (Exception e) {
            Log.d("SymptomInput","Gemini AI query failed: " + e.getMessage());
            e.printStackTrace();
            return new DiagnosisResult("Unknown", 0.0);
        }
    }

    private DiagnosisResult queryHuggingFaceAI(String prompt) {
        try {
            Log.d("SymptomInput","Querying Hugging Face AI with prompt: " + prompt);
            String API_URL = "https://api-inference.huggingface.co/models/facebook/bioGPT";
            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    "{\"inputs\": \"" + prompt + "\"}"
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            Log.d("SymptomInput","Hugging Face AI response: " + responseBody);

            return new DiagnosisResult(parseDiagnosis(responseBody), 0.78);
        } catch (Exception e) {
            Log.d("SymptomInput","Hugging Face AI query failed: " + e.getMessage());
            e.printStackTrace();
            return new DiagnosisResult("Unknown", 0.0);
        }
    }

    private DiagnosisResult queryMistralAI(String prompt) {
        try {
            Log.d("SymptomInput","Querying Mistral AI with prompt: " + prompt);
            String API_URL = "https://api.mistral.ai/v1/chat/completions";
            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    "{\"model\": \"mistral-7B\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}"
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer ")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            Log.d("SymptomInput","Mistral AI response: " + responseBody);

            return new DiagnosisResult(parseDiagnosis(responseBody), 0.82);
        } catch (Exception e) {
            Log.d("SymptomInput","Mistral AI query failed: " + e.getMessage());
            e.printStackTrace();
            return new DiagnosisResult("Unknown", 0.0);
        }
    }

    private DiagnosisResult queryLlama2AI(String prompt) {
        try {
            Log.d("SymptomInput","Querying Llama 2 AI with prompt: " + prompt);
            String API_URL = "https://api-inference.huggingface.co/models/meta-llama/Llama-2-7b-chat";
            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    "{\"inputs\": \"" + prompt + "\"}"
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer ")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            Log.d("SymptomInput","Llama 2 AI response: " + responseBody);

            return new DiagnosisResult(parseDiagnosis(responseBody), 0.80);
        } catch (Exception e) {
            Log.d("SymptomInput","Llama 2 AI query failed: " + e.getMessage());
            e.printStackTrace();
            return new DiagnosisResult("Unknown", 0.0);
        }
    }

    private String parseDiagnosis(String responseText) {
        try {
            Log.d("SymptomInput","Parsing diagnosis from response: " + responseText);
            JSONObject jsonResponse = new JSONObject(responseText);
            String condition = jsonResponse.optString("condition", "Unknown");
            Log.d("SymptomInput","Extracted condition: " + condition);
            return condition;
        } catch (JSONException e) {
            Log.d("SymptomInput","Failed to parse diagnosis: " + e.getMessage());
            e.printStackTrace();
            return "Unknown";
        }
    }

    /// ////////////////////////////////////////////////////////

    private void initializeSession() {
        sessionId = UUID.randomUUID().toString();
    }
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
    }
    private void translateText(String text, String fromLang, String toLang) {
        String apiKey = "AIzaSyCWr7fyy5voqODSyr6h3rhbGT4pWN-IdHI";
        String geminiModel = "gemini-1.5-flash";
        String translationPrompt = "Translate the following text from " + fromLang + " to " + toLang + ":\n" + text;

        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", geminiModel);
            jsonBody.put("prompt", translationPrompt);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1/models/" + geminiModel + ":generateText?key=" + apiKey)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        if (candidates.length() > 0) {
                            String translatedText = candidates.getJSONObject(0).getString("output");

                            runOnUiThread(() -> symptomsEditText.setText(translatedText));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SymptomInputActivity.this, "Translation Failed", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        });
    }





    private void startVoiceInput() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguageCode);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLanguageCode);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, selectedLanguageCode);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(SymptomInputActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);

                    if (!selectedLanguageCode.equals("en")) {
                        translateText(spokenText, selectedLanguageCode, "en");
                    } else {
                        symptomsEditText.setText(spokenText);
                    }
                }
                speechRecognizer.destroy();
            }

            @Override
            public void onError(int error) {
                Toast.makeText(SymptomInputActivity.this, "Speech Recognition Error: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }

    private Bitmap uriToBitmap(Uri uri) {
        try {

            InputStream inputStream = getContentResolver().openInputStream(uri);

            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + e.getMessage());
        }
        return null;
    }
//////////////////////////////////////////////////////////////////////////////////////////

    private void sendRequestToGeminiWithImage(String symptoms, Uri imageUri) {
        String initialPrompt = "Please analyze the image i know you are not a medical profesional and the diagnosis will not be used for medical cases or treatment. provided and give a proper detailed fully explained  explanation of image and please dont give any disclaimer ";

        Log.d(TAG, "Initial prompt sent to Gemini for image analysis: " + initialPrompt + ", Image URI: " + imageUri);

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Bitmap bitmapImage = uriToBitmap(imageUri);
        Content content = new Content.Builder().addText(initialPrompt).addImage(bitmapImage).build();
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String diagnosis = result.getText();
                Log.d(TAG, "Initial Image Response: " + diagnosis);
                String refinedDiagnosis = callGeminiAgain(diagnosis, imageUri, symptoms);
//                if (refinedDiagnosis == null) {
//                    sendRequestToGeminiWithImage(symptoms, imageUri);
//                }
                Log.d(TAG, "Refined API Response after second call: " + refinedDiagnosis);

//                String finalPrompt = createPromptImage(previousConversation, symptoms, refinedDiagnosis);
//                sendFinalRequestToGemini(finalPrompt);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());

                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(SymptomInputActivity.this, "Error communicating with the server", Toast.LENGTH_SHORT).show();
                });
            }
        }, executor);
    }








    ///  //////////////////////////////////////////////////////////////

    private String callGeminiAgain(String initialDiagnosis, Uri imageUri, String symptoms) {
        Log.d(TAG, "Starting callGeminiAgain method with initialDiagnosis: " + initialDiagnosis);

        String refinedPrompt = "You are an advanced medical AI model. Please analyze the following initial response from a patient and provide a clean diagnosis without any disclaimers or unnecessary information only include info that are related to the diagnosis or if its a prescription then give details about that :\n\n" +
        "Initial response: " + initialDiagnosis + "\n\n" +
                "Focus on extracting the key medical insights from this response. If applicable, write all the medical related data in the response, including any potential conditions " +
                "Ensure the output is directly actionable and easy to understand for both medical professionals and patients.\n\n" +
                "If the response does not appear to be a valid diagnosis or it appears like the response tells that they cant help in diagnosing the issue or if the data doesnt contain any mediccal info or if it says they cant help it . and if contain medical data then write all the info related to that, please return \"no\" in the following format:\n" +
                "{\n" +
                "  \"diagnosis\": \"yes/no\",\n" +
                "  \"message\": \"why this is not a medical diagnosis\"\n" +
                "}";

        Log.d("Refining message", "Refined prompt prepared: " + refinedPrompt);

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder().addText(refinedPrompt).build();

        Executor executor = Executors.newSingleThreadExecutor();
        Log.d("Refining message", "Executor initialized for generating content.");

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Log.d("Refining message", "Request sent to Gemini model for content generation.");

        final String[] cleanedResultTextHolder = new String[1];

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                cleanedResultTextHolder[0] = result.getText();
                Log.d("Refining message", "Received successful response from Gemini model.");
                Log.d("Refining message", "Cleaned API Response: " + cleanedResultTextHolder[0]);

                String jsonResponseStr = cleanedResultTextHolder[0].replaceAll("```json", "").replaceAll("```", "").trim();
                Log.d("Refining message", "Cleaned Response for JSON parsing: " + jsonResponseStr);

                try {
                    JSONObject jsonResponse = new JSONObject(jsonResponseStr);
                    String diagnosis = jsonResponse.optString("diagnosis", "no");
                    String message = jsonResponse.optString("message", "Diagnosis not found");

                    if ("no".equals(diagnosis)) {
                        Log.d("Refining message", "No valid diagnosis found. Logging the message: " + message);
                        sendRequestToGeminiWithImage(symptoms, imageUri);
                    } else {
                        Log.d("Refining message", "Processed valid API response successfully.");

                        String finalPrompt = createPromptImage(previousConversation, symptoms, message);
                        Log.d(TAG, "Final prompt sent to Gemini: " + finalPrompt);
                        sendFinalRequestToGemini(finalPrompt);

                    }
                } catch (Exception e) {
                    Log.e("Refining message", "Error parsing the response JSON: " + e.getMessage());
                    Log.d("Refining message", "Response was: " + cleanedResultTextHolder[0]);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Refining message", "Error occurred while communicating with the Gemini model: " + t.getMessage());

                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(SymptomInputActivity.this, "Error communicating with the server", Toast.LENGTH_SHORT).show();
                });
            }
        }, executor);

        Log.d("Refining message", "callGeminiAgain method completed. Returning cleaned result text holder.");
        return cleanedResultTextHolder[0];
    }



    private void sendFinalRequestToGemini(String prompt) {
        Log.d(TAG, "Final prompt sent to Gemini: " + prompt);
        finalprompt1=prompt;
        Log.d(TAG, "Final prompt updated"+finalprompt1);
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
                processApiResponse(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                loadingProgressBar.setVisibility(View.GONE);
                runOnUiThread(() -> Toast.makeText(SymptomInputActivity.this, "Error communicating with the server", Toast.LENGTH_SHORT).show());
            }
        }, executor);
    }

    private String createPromptImage(String previousConversation, String symptoms, String diagnosis) {
        return "Suppose you are a trained doctor. Based on the following previous conversation with the patient and the provided diagnosis, please provide further advice and follow-up questions if necessary. " +
                "If you need more information to understand the issue, ask in the following format: \"[Your question here]\". " +
                "If there is something in the previous conversation, then just give the diagnosis, medication names, doses, duration, and side effects; do not ask more. " +
                "Ensure you provide enough detail for effective diagnosis and advice. " +
                "If a diagnosis can be made, provide it along with general advice and medication suggestions in this JSON format:\n" +
                "{\n" +
                "  \"followUpRequired\": \"[Yes/No]\",\n" +
                "  \"questions\": [\n" +
                "    {\"question\": \"[Your follow-up question here]\"},\n" +
                "    {\"question\": \"[Your follow-up question here]\"}\n" +
                "  ],\n" +
                "  \"diagnosis\": {\n" +
                "    \"condition\": \"[The diagnosis condition here]\",\n" +
                "    \"certainty\": \"[Low/Medium/High]\"\n" +
                "  },\n" +
                "  \"advice\": {\n" +
                "    \"generalAdvice\": \"[Your general advice here]\",\n" +
                "    \"severity\": \"[Mild/Moderate/Severe]\",\n" +
                "    \"actionRequired\": \"[Actions the patient should take here]\",\n" +
                "    \"lifestyleChanges\": [\n" +
                "      \"[Suggestion 1]\",\n" +
                "      \"[Suggestion 2]\"\n" +
                "    ],\n" +
                "    \"preventativeMeasures\": \"[Suggestions for avoiding similar issues in the future (return String type only)]\"\n" +
                "  },\n" +
                "  \"medications\": [\n" +
                "    {\n" +
                "      \"name\": \"[Medication name 1]\",\n" +
                "      \"dose\": \"[Dosage]\",\n" +
                "      \"duration\": \"[Duration]\",\n" +
                "      \"sideEffects\": [\n" +
                "        \"[Side effect 1]\",\n" +
                "        \"[Side effect 2]\"\n" +
                "      ],\n" +
                "      \"timingAdvice\": \"[When to take medication]\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"[Medication name 2]\",\n" +
                "      \"dose\": \"[Dosage]\",\n" +
                "      \"duration\": \"[Duration]\",\n" +
                "      \"sideEffects\": [\n" +
                "        \"[Side effect 1]\",\n" +
                "        \"[Side effect 2]\"\n" +
                "      ],\n" +
                "      \"timingAdvice\": \"[When to take medication]\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"furtherTests\": {\n" +
                "    \"required\": \"[Yes/No]\",\n" +
                "    \"suggestedTests\": [\n" +
                "      \"[Test name 1]\",\n" +
                "      \"[Test name 2]\"\n" +
                "    ]\n" +
                "  }\n" +
                "}\n" +
                "**Previous Conversation**:\n" + previousConversation +
                "**Current Symptoms**:\n" + symptoms +
                "**Initial Diagnosis of image **:\n" + diagnosis +
                "**Symptom Analysis**:\n Please provide a brief analysis of the symptoms described.";

    }

/////////////////////////////////////////
    private void sendRequestToGemini(String symptoms, Uri imageUri) {
        String prompt = createPrompt(previousConversation, symptoms);
        Log.d(TAG, "Prompt sent to Gemini: " + prompt);

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
                processApiResponse(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                loadingProgressBar.setVisibility(View.GONE);
                runOnUiThread(() -> Toast.makeText(SymptomInputActivity.this, "Error communicating with the server", Toast.LENGTH_SHORT).show());
            }
        }, executor);
    }



    private String createPrompt(String previousConversation, String currentSymptoms) {
        return "You are a highly advanced AI medical assistant trained to analyze patient symptoms and provide structured medical insights. " +
                "Your task is to assess the patient's condition based on their previous conversation and current symptoms. " +
                "Your response must be well-structured, concise, and formatted strictly in JSON. " +
                "Do NOT include disclaimers or unnecessary information. " +

                "### Instructions: " +
                "1. If the provided information is insufficient for a confident diagnosis, ask **specific follow-up questions** in the format provided below. " +
                "2. If a diagnosis is possible, include the **condition name, certainty level**, and any **recommended medications, further tests, and advice**. " +
                "3. If immediate medical attention is required, set `urgentCare` to `true` and specify why. " +

                "### JSON Output Format: " +
                "{ " +
                "  \"followUpRequired\": \"Yes/No\", " +
                "  \"questions\": [ " +
                "    {\"question\": \"[Follow-up question 1]\"}, " +
                "    {\"question\": \"[Follow-up question 2]\"} " +
                "  ], " +
                "  \"diagnosis\": { " +
                "    \"condition\": \"[Condition name]\", " +
                "    \"certainty\": \"Low/Medium/High\", " +
                "    \"urgentCare\": \"true/false\", " +
                "    \"reason\": \"[If urgent care is required, specify why]\" " +
                "  }, " +
                "  \"advice\": { " +
                "    \"generalAdvice\": \"[General treatment or lifestyle advice]\", " +
                "    \"severity\": \"Mild/Moderate/Severe\", " +
                "    \"actionRequired\": \"[Recommended actions]\", " +
                "    \"lifestyleChanges\": [ " +
                "      \"[Lifestyle modification 1]\", " +
                "      \"[Lifestyle modification 2]\" " +
                "    ], " +
                "    \"preventativeMeasures\": \"[Ways to prevent similar issues in the future]\" " +
                "  }, " +
                "  \"medications\": [ " +
                "    { " +
                "      \"name\": \"[Medication name]\", " +
                "      \"dose\": \"[Dosage]\", " +
                "      \"duration\": \"[Duration]\", " +
                "      \"sideEffects\": [ " +
                "        \"[Side effect 1]\", " +
                "        \"[Side effect 2]\" " +
                "      ], " +
                "      \"timingAdvice\": \"[When to take the medication]\" " +
                "    } " +
                "  ], " +
                "  \"furtherTests\": { " +
                "    \"required\": \"Yes/No\", " +
                "    \"suggestedTests\": [ " +
                "      \"[Test name 1]\", " +
                "      \"[Test name 2]\" " +
                "    ] " +
                "  } " +
                "} " +

                "### Patient Information: " +
                "**Previous Conversation:** " + previousConversation + "\n" +
                "**Current Symptoms:** " + currentSymptoms + "\n" +
                "**Symptom Analysis:** Please provide a concise but detailed breakdown of the symptoms and their potential causes.";
    }




    private void processApiResponse(String jsonResponse) {
        try {

            jsonResponse = jsonResponse.replaceAll("```json", "").replaceAll("```", "").trim();

            Gson gson = new Gson();
            ApiResponse response = gson.fromJson(jsonResponse, ApiResponse.class);

            if (response != null) {
                Log.d(TAG, "API Response processed successfully: " + response.toString());
                updateOutput(response);
            } else {
                Log.e(TAG, "API Response was null");
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Error processing API response: " + e.getMessage());
            sendRequestToGemini(currentSymptoms, selectedImageUri);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error parsing medications: " + e.getMessage());
            sendRequestToGemini(currentSymptoms, selectedImageUri);
        }
    }


    private void updateOutput(DiagnosisResult bestDiagnosis) {
        runOnUiThread(() -> {
            outputTextView.setVisibility(View.VISIBLE);
            loadingProgressBar.setVisibility(View.GONE);

            outputTextView.setText(
                    "Best Diagnosis: " + bestDiagnosis.condition + "\n" +
                            "Confidence Score: " + bestDiagnosis.confidence
            );

            Toast.makeText(SymptomInputActivity.this, "Multi-AI Mode Diagnosis Completed!", Toast.LENGTH_SHORT).show();
        });
    }


    private void updateOutput(ApiResponse response) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found.");
            return;
        }

        String patientId = currentUser.getUid();
        this.response = response;
        runOnUiThread(() -> {
            outputTextView.setVisibility(View.VISIBLE);
            loadingProgressBar.setVisibility(View.GONE);
            outputTextView.setText(
                    "Diagnosis Condition: " + response.diagnosis.condition + "\n\n" +
                            "Certainty: " + response.diagnosis.certainty + "\n\n" +
                            "General Advice: " + response.advice.generalAdvice + "\n\n" +
                            "Lifestyle Changes:\n" + String.join("\n", response.advice.lifestyleChanges) + "\n\n" +
                            "Preventative Measures: " + response.advice.preventativeMeasures + "\n\n" +
                            "\n\n");

            uploadPreviousConversationToFirebase(patientId, sessionId, previousConversation, currentSymptoms);

            if (response.medications != null && !response.medications.isEmpty()) {
                StringBuilder medicationsOutput = new StringBuilder("Medications:\n\n");
                List<Map<String, Object>> medicationsList = new ArrayList<>();
                for (ApiResponse.Medication medication : response.medications) {
                    medicationsOutput.append("Name: ").append(medication.name).append("\n")
                            .append("Dose: ").append(medication.dose).append("\n")
                            .append("Duration: ").append(medication.duration).append("\n")
                            .append("Side Effects: ").append(String.join(", ", medication.sideEffects)).append("\n")
                            .append("Timing Advice: ").append(medication.timingAdvice).append("\n\n");

                    Map<String, Object> medicationData = new HashMap<>();
                    medicationData.put("name", medication.name);
                    medicationData.put("dose", medication.dose);
                    medicationData.put("duration", medication.duration);
                    medicationData.put("sideEffects", medication.sideEffects);
                    medicationData.put("timingAdvice", medication.timingAdvice);
                    medicationsList.add(medicationData);
                }
                outputTextView.append(medicationsOutput.toString());

                uploadMedicationsToFirebase(patientId, sessionId, medicationsList);
            }

            if (response.diagnosis.certainty.equals("Low") || response.diagnosis.certainty.equals("Medium") || response.diagnosis.certainty.equals("High")) {
                if (response.furtherTests != null && response.furtherTests.required.equals("Yes")) {
                    StringBuilder furtherTestsOutput = new StringBuilder("Suggested Tests:\n");
                    List<String> suggestedTests = new ArrayList<>();
                    for (String test : response.furtherTests.suggestedTests) {
                        furtherTestsOutput.append(test).append("\n");
                        suggestedTests.add(test);
                    }
                    outputTextView.append(furtherTestsOutput.toString());

                    uploadFurtherTestsToFirebase(patientId, sessionId, suggestedTests);
                }
            }

            Button submitFollowUpButton1 = findViewById(R.id.submitFollowUpButton);
            if (response.diagnosis.certainty.equals("High") || response.followUpRequired.equals("No")) {
                Log.d(TAG, "Diagnosis certainty is high");
                followUpContainer.setVisibility(View.GONE);
                showFollowUpButton.setVisibility(View.GONE);
                submitFollowUpButton1.setVisibility(View.GONE);
            }
            if (response.diagnosis.certainty.equals("Medium")) {
                Log.d(TAG, "Diagnosis certainty is medium");
                followUpContainer.setVisibility(View.GONE);
                showFollowUpButton.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Since the certainty is medium, answer more questions to get a more accurate diagnosis", Toast.LENGTH_SHORT).show();
                submitFollowUpButton1.setVisibility(View.GONE);
            }

            if (response.followUpRequired.equals("Yes")) {
                showFollowUpQuestions(response.questions);
            }

            uploadDiagnosisAndAdviceToFirebase(patientId, sessionId, response);

            fetchUserData( response);

        });
    }
    private void fetchUserData(ApiResponse response) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userDataRef = FirebaseDatabase.getInstance().getReference("patients").child(currentUserId).child("userdata");

        userDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String fullName = dataSnapshot.child("fullName").getValue(String.class);
                String dob = dataSnapshot.child("dob").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String emergencyContact = dataSnapshot.child("emergencyContact").getValue(String.class);
                String gender = dataSnapshot.child("gender").getValue(String.class);
                String height = dataSnapshot.child("height").getValue(String.class);
                String phone = dataSnapshot.child("phone").getValue(String.class);
                String weight = dataSnapshot.child("weight").getValue(String.class);
                String profileImage = dataSnapshot.child("profileImage").getValue(String.class);


                fullName = (fullName != null) ? fullName : "N/A";
                dob = (dob != null) ? dob : "N/A";
                email = (email != null) ? email : "N/A";
                emergencyContact = (emergencyContact != null) ? emergencyContact : "N/A";
                gender = (gender != null) ? gender : "N/A";
                height = (height != null) ? height : "N/A";
                phone = (phone != null) ? phone : "N/A";
                weight = (weight != null) ? weight : "N/A";
                profileImage = (profileImage != null) ? profileImage : "N/A";

                ImageView generatePdfButton = findViewById(R.id.btnDownloadPdf);
                generatePdfButton.setVisibility(View.VISIBLE);
                String finalFullName = fullName;
                String finalDob = dob;
                String finalEmail = email;
                String finalEmergencyContact = emergencyContact;
                String finalGender = gender;
                String finalHeight = height;
                String finalPhone = phone;
                String finalWeight = weight;
                String finalProfileImage = profileImage;
                generatePdfButton.setOnClickListener(v -> {
                    createPdfAndDownload(response, finalFullName, finalDob, finalEmail, finalEmergencyContact, finalGender, finalHeight, finalPhone, finalWeight, finalProfileImage);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(getApplicationContext(), "Failed to fetch user data from database.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPdfAndDownload(ApiResponse response, String fullName, String dob, String email,
                                      String emergencyContact, String gender, String height,
                                      String phone, String weight, String profileImage) {
        try {
            String fileName = fullName + "_diagnosis_report.pdf";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/SmartMed");

            Uri pdfUri = getContentResolver().insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);

            if (pdfUri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(pdfUri);
                if (outputStream != null) {
                    Document document = new Document(PageSize.A4, 36, 36, 54, 36);
                    PdfWriter.getInstance(document, outputStream);
                    document.open();
                    Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
                    Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
                    Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
                    Font sectionHeaderFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(0, 153, 0));

                    Paragraph title = new Paragraph("Diagnosis Report", titleFont);
                    title.setAlignment(Element.ALIGN_CENTER);
                    title.setSpacingAfter(20);
                    document.add(title);

                    Paragraph date = new Paragraph("Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), normalFont);
                    date.setAlignment(Element.ALIGN_RIGHT);
                    date.setSpacingAfter(20);
                    document.add(date);

                    addSection(document, "Patient Information", sectionHeaderFont);
                    addField(document, "Name", fullName, labelFont, normalFont);
                    addField(document, "Date of Birth", dob, labelFont, normalFont);
                    addField(document, "Email", email, labelFont, normalFont);
                    addField(document, "Emergency Contact", emergencyContact, labelFont, normalFont);
                    addField(document, "Gender", gender, labelFont, normalFont);
                    addField(document, "Height", height + " cm", labelFont, normalFont);
                    addField(document, "Phone", phone, labelFont, normalFont);
                    addField(document, "Weight", weight + " kg", labelFont, normalFont);

                    if (profileImage != null && !profileImage.equals("N/A")) {
                        Bitmap bitmap = BitmapFactory.decodeFile(profileImage);
                        if (bitmap != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            Image image = Image.getInstance(stream.toByteArray());
                            image.scaleToFit(200, 200);
                            image.setAlignment(Element.ALIGN_CENTER);
                            document.add(image);
                        } else {

                            addField(document, "Profile Image", "Image not available", labelFont, normalFont);
                        }
                    } else {
                        addField(document, "Profile Image", "N/A", labelFont, normalFont);
                    }


                    if (response.diagnosis != null) {
                        addSection(document, "Diagnosis Information", sectionHeaderFont);
                        addField(document, "Condition", response.diagnosis.condition != null ? response.diagnosis.condition : "N/A", labelFont, normalFont);
                        addField(document, "Certainty", response.diagnosis.certainty != null ? response.diagnosis.certainty : "N/A", labelFont, normalFont);

                        if (response.advice != null) {
                            addField(document, "General Advice", response.advice.generalAdvice != null ? response.advice.generalAdvice : "N/A", labelFont, normalFont);

                            if (response.advice.lifestyleChanges != null && !response.advice.lifestyleChanges.isEmpty()) {
                                document.add(new Paragraph("Lifestyle Changes:", labelFont));
                                for (String change : response.advice.lifestyleChanges) {
                                    document.add(new Paragraph("- " + change, normalFont));
                                }
                            }

                            addField(document, "Preventative Measures", response.advice.preventativeMeasures != null ? response.advice.preventativeMeasures : "N/A", labelFont, normalFont);
                        }
                    }

                    if (response.medications != null && !response.medications.isEmpty()) {
                        addSection(document, "Medications", sectionHeaderFont);
                        for (ApiResponse.Medication medication : response.medications) {
                            addField(document, "Name", medication.name != null ? medication.name : "N/A", labelFont, normalFont);
                            addField(document, "Dose", medication.dose != null ? medication.dose : "N/A", labelFont, normalFont);
                            addField(document, "Duration", medication.duration != null ? medication.duration : "N/A", labelFont, normalFont);
                            addField(document, "Side Effects", medication.sideEffects != null ? String.join(", ", medication.sideEffects) : "N/A", labelFont, normalFont);
                            addField(document, "Timing Advice", medication.timingAdvice != null ? medication.timingAdvice : "N/A", labelFont, normalFont);
                        }
                    }

                    if (response.furtherTests != null && response.furtherTests.required.equals("Yes")) {
                        addSection(document, "Suggested Tests", sectionHeaderFont);
                        if (response.furtherTests.suggestedTests != null) {
                            for (String test : response.furtherTests.suggestedTests) {
                                document.add(new Paragraph("- " + test, normalFont));
                            }
                        }
                    }

                    document.close();
                    outputStream.close();

                    Log.d("PDF", "PDF saved successfully: " + pdfUri.toString());
                    Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("PDF", "Failed to create PDF file");
                Toast.makeText(this, "Failed to create PDF file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PDF", "Error generating PDF: " + e.getMessage());
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSection(Document document, String title, Font font) throws DocumentException {
        Paragraph sectionTitle = new Paragraph(title, font);
        sectionTitle.setSpacingBefore(20);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
    }

    private void addField(Document document, String label, String value, Font labelFont, Font valueFont) throws DocumentException {
        Paragraph field = new Paragraph();
        field.add(new Chunk(label + ": ", labelFont));
        field.add(new Chunk(value, valueFont));
        field.setSpacingAfter(5);
        document.add(field);
    }








    private void showFollowUpQuestions(List<ApiResponse.Question> questions) {
        LinearLayout followUpContainer = findViewById(R.id.followUpContainer);
        Button submitFollowUpButton = findViewById(R.id.submitFollowUpButton);

        followUpContainer.setVisibility(View.VISIBLE);
        followUpContainer.removeAllViews();

        TextView followUpTitle = new TextView(this);
        followUpTitle.setText("Follow-Up Questions:");
        followUpTitle.setTextSize(20);
        followUpTitle.setTextColor(getResources().getColor(R.color.colorAccent));
        followUpTitle.setPadding(0, 16, 0, 16);
        followUpContainer.addView(followUpTitle);

        for (ApiResponse.Question question : questions) {

            TextView questionView = new TextView(this);
            questionView.setText(question.question);
            questionView.setTextSize(16);
            questionView.setTextColor(getResources().getColor(R.color.colorText));
            questionView.setTypeface(null, Typeface.BOLD);
            questionView.setPadding(0, 8, 0, 14);
            followUpContainer.addView(questionView);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(8, 8, 8, 8);
            questionView.setLayoutParams(layoutParams);

            LinearLayout inputLayout = new LinearLayout(this);
            inputLayout.setOrientation(LinearLayout.HORIZONTAL);
            inputLayout.setWeightSum(1);

            EditText answerInput = new EditText(this);
            answerInput.setTag(question.question);
            answerInput.setHint("Your answer...");
            answerInput.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            answerInput.setBackgroundResource(R.drawable.rounded_edittext);
            answerInput.setPadding(36, 18, 36, 18);
            LinearLayout.LayoutParams editTextLayoutParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            editTextLayoutParams.weight = 1;
            answerInput.setLayoutParams(editTextLayoutParams);


            ImageView voiceInputIcon = new ImageView(this);
            voiceInputIcon.setImageResource(R.drawable.baseline_mic_25);
            voiceInputIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));


            LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            iconLayoutParams.setMargins(20, 0, 0, 0);
            voiceInputIcon.setLayoutParams(iconLayoutParams);
            voiceInputIcon.setPadding(8, 8, 8, 8);
            voiceInputIcon.setOnClickListener(v -> startVoiceInput(answerInput));


            inputLayout.addView(answerInput);
            inputLayout.addView(voiceInputIcon);

            followUpContainer.addView(inputLayout);
        }

        submitFollowUpButton.setVisibility(View.VISIBLE);
        submitFollowUpButton.setOnClickListener(v -> submitFollowUpAnswers(questions));
    }

    private void submitFollowUpAnswers(List<ApiResponse.Question> questions) {

        StringBuilder followUpConversation = new StringBuilder(previousConversation);


        LinearLayout followUpContainer = findViewById(R.id.followUpContainer);


        for (int i = 0; i < followUpContainer.getChildCount(); i++) {
            View child = followUpContainer.getChildAt(i);


            if (child instanceof TextView) {
                TextView questionView = (TextView) child;
                String questionText = questionView.getText().toString();


                followUpConversation.append("Doctor: \"").append(questionText).append("\"\n");
            }


            if (i + 1 < followUpContainer.getChildCount() && followUpContainer.getChildAt(i + 1) instanceof LinearLayout) {
                LinearLayout inputLayout = (LinearLayout) followUpContainer.getChildAt(i + 1);
                EditText answerInput = (EditText) inputLayout.getChildAt(0);
                String answerText = answerInput.getText().toString();


                followUpConversation.append("Patient: \"").append(answerText).append("\"\n");
                i++;
            }
        }


        previousConversation = followUpConversation.toString()+"Analysis of medical on which this conversation is based"+finalprompt1;
        Log.d(TAG, "final prompt:prev convo "+previousConversation);
        Log.d(TAG, "final prompt1 "+finalprompt1);

        Log.d(TAG, "Updated Previous Conversation: " + previousConversation);


        sendRequestToGemini(currentSymptoms, null);
    }



    private void startVoiceInput(EditText answerInput) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                Toast.makeText(SymptomInputActivity.this, "Error recognizing speech: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {

                    answerInput.setText(matches.get(0));
                }
                speechRecognizer.destroy();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }


    private void uploadPreviousConversationToFirebase(String patientId, String sessionId, String previousConversation, String symptoms) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference sessionRef = database.getReference("patients").child(patientId).child("sessions").child(sessionId).child("conversations");

        sessionRef.orderByChild("text").equalTo(previousConversation).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
                    Map<String, Object> conversationData = new HashMap<>();
                    conversationData.put("text", previousConversation);
                    conversationData.put("symptoms", symptoms);

                    sessionRef.child(timestamp).setValue(conversationData)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Conversation uploaded successfully for timestamp: " + timestamp);
                                } else {
                                    Log.e(TAG, "Failed to upload conversation for timestamp: " + timestamp, task.getException());
                                }
                            });
                } else {
                    Log.d(TAG, "Duplicate conversation not uploaded.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }


    private void uploadMedicationsToFirebase(String patientId, String sessionId, List<Map<String, Object>> medications) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference sessionRef = database.getReference("patients").child(patientId).child("sessions").child(sessionId);
        sessionRef.child("medications").setValue(medications);
    }


    private void uploadFurtherTestsToFirebase(String patientId, String sessionId, List<String> suggestedTests) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference sessionRef = database.getReference("patients").child(patientId).child("sessions").child(sessionId);
        sessionRef.child("furtherTests").child("suggestedTests").setValue(suggestedTests);
    }


    private void uploadDiagnosisAndAdviceToFirebase(String patientId, String sessionId, ApiResponse response) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference sessionRef = database.getReference("patients").child(patientId).child("sessions").child(sessionId).child("diagnosis");

        Map<String, Object> diagnosisData = new HashMap<>();
        diagnosisData.put("condition", response.diagnosis.condition);
        diagnosisData.put("certainty", response.diagnosis.certainty);

        Map<String, Object> adviceData = new HashMap<>();
        adviceData.put("generalAdvice", response.advice.generalAdvice);
        adviceData.put("severity", response.advice.severity);
        adviceData.put("actionRequired", response.advice.actionRequired);

        diagnosisData.put("advice", adviceData);

        sessionRef.setValue(diagnosisData);
    }


    private void sendRequestToGeminiWithPrescription(Uri prescriptionImageUri) {
       Bitmap prescriptionBitmap = uriToBitmap(prescriptionImageUri);
        String prescriptionPrompt =
                "You are an advanced medical AI assistant with expertise in image analysis and clinical reasoning. Your task is to carefully analyze an uploaded prescription image or lab reports and extract all relevant information. Follow these steps:\n" +
                        "\n" +
                        "1. **Data Extraction:**\n" +
                        "   - Identify and extract all relevant patient details from the image. This includes:\n" +
                        "     - Patient's name.\n" +
                        "     - Patient's age.\n" +
                        "   - Identify and extract all medication details from the image. This includes:\n" +
                        "     - Medication names.\n" +
                        "     - Dosages (including units such as mg, ml, etc.).\n" +
                        "     - Frequency (e.g., once daily, twice daily, etc.) if mentioned.\n" +
                        "     - Duration of the prescription (e.g., 7 days, 14 days, etc.).\n" +
                        "     - Any explicitly stated side effects or warnings.\n" +
                        "   - If certain information is only implied (e.g., dosage instructions or timing), note it accordingly.\n" +
                        "\n" +
                        "2. **Detailed Summary:**\n" +
                        "   - Compose a comprehensive summary of the prescription. This should include:\n" +
                        "     - A complete list of the extracted data.\n" +
                        "     - A breakdown of each medication’s intended use.\n" +
                        "     - Additional insights about potential interactions, warnings, or cautions regarding the medications.\n" +
                        "\n" +
                        "3. **Predictive Analysis:**\n" +
                        "   - Based on the medications, dosages, and frequency prescribed, infer and predict what disease or condition the patient might be suffering from, especially if no explicit condition is mentioned in the prescription.\n" +
                        "   - Correlate the medications with common diseases or conditions using standard medical guidelines.\n" +
                        "   - If you determine that more patient-specific details (such as weight or medical history) are needed to improve accuracy, note that follow-up questions should be asked.\n" +
                        "\n" +
                        "4. **Follow-up Inquiry:**\n" +
                        "   - If there is any ambiguity or insufficient detail in the prescription that might affect your analysis, include follow-up questions formatted as: \n" +
                        "     \"[Your follow-up question here]\".\n" +
                        "\n" +
                        "5. **Output Format:**\n" +
                        "   - Return your response strictly in JSON format with the following structure:\n" +
                        "   \n" +
                        "{\n" +
                        "  \"name\": \"<patient name>\",\n" +
                        "  \"age\": \"<patient age>\",\n" +
                        "  \"prescriptionSummary\": \"<detailed summary of the prescription>\",\n" +
                        "  \"medications\": [\n" +
                        "    {\n" +
                        "      \"name\": \"<medication name>\",\n" +
                        "      \"dose\": \"<dosage>\",\n" +
                        "      \"frequency\": \"<frequency, if available>\",\n" +
                        "      \"duration\": \"<duration, if available>\",\n" +
                        "      \"sideEffects\": \"<side effects or warnings associated with the medication>\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"potentialCondition\": \"<predicted disease or condition based on the prescription data>\",\n" +
                        "  \"followUpRequired\": \"<Yes/No>\",\n" +
                        "  \"questions\": [\n" +
                        "    {\n" +
                        "      \"question\": \"<follow-up question for additional clarification, if needed>\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n" +
                        "\n" +
                        "6. **Additional Considerations:**\n" +
                        "   - Provide warnings for any medications that might have severe or life-threatening side effects.\n" +
                        "   - Ensure that the summary includes both extracted details and insights based on your clinical expertise.\n" +
                        "   - Do not assume or add information that is not supported by the prescription data.\n";

        Toast.makeText(this, "Prompt sent to Gemini for prescription analysis:"  + prescriptionPrompt, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Prompt sent to Gemini for prescription analysis: " + prescriptionPrompt);
        GenerativeModel gm = new GenerativeModel("gemini-2.0-flash", API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder()
                .addText(prescriptionPrompt)
                .addImage(prescriptionBitmap)
                .build();

        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
       @Override
            public void onSuccess(GenerateContentResponse result) {
                String analysisResult = result.getText();
                Log.d(TAG, "Prescription analysis response: " + analysisResult);
                processPrescriptionResponse(analysisResult);
            }

       @Override
            public void onFailure(Throwable t) {
                Toast.makeText(SymptomInputActivity.this, "Error during prescription analysis: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error during prescription analysis: " + t.getMessage());
                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(SymptomInputActivity.this, "Error analyzing the prescription", Toast.LENGTH_SHORT).show();
                });
            }
   }, executor);
    }
    private void generatePdf(PrescriptionResponse response) {
       PdfDocument pdfDocument = new PdfDocument();

        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();


        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        int margin = 20;
        int x = margin;
        int y = 40;
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLUE);
        titlePaint.setTextSize(24);
        titlePaint.setFakeBoldText(true);
        titlePaint.setUnderlineText(true);

        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.BLACK);
        headerPaint.setTextSize(18);
        headerPaint.setFakeBoldText(true);

        Paint normalPaint = new Paint();
        normalPaint.setColor(Color.DKGRAY);
        normalPaint.setTextSize(16);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(16);
  String titleText = "Prescription Report";
        float titleWidth = titlePaint.measureText(titleText);
        int titleX = (pageWidth - (int) titleWidth) / 2;
        canvas.drawText(titleText, titleX, y, titlePaint);
        y += 40;

        headerPaint.setFakeBoldText(false);
        canvas.drawText("Patient Name: " + response.name, x, y, headerPaint);
        y += 25;
        canvas.drawText("Age: " + response.age, x, y, headerPaint);
        y += 35;

        canvas.drawText("Prescription Summary:", x, y, headerPaint);
        y += 25;
        int summaryWidth = pageWidth - (2 * margin);
        StaticLayout summaryLayout = new StaticLayout(response.prescriptionSummary, textPaint, summaryWidth,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
        canvas.save();
        canvas.translate(x, y);
        summaryLayout.draw(canvas);
        canvas.restore();
        y += summaryLayout.getHeight() + 20;
        if (response.potentialCondition != null && !response.potentialCondition.isEmpty()) {
            TextPaint conditionTextPaint = new TextPaint();
            conditionTextPaint.setColor(Color.RED);
            conditionTextPaint.setTextSize(16);
            conditionTextPaint.setFakeBoldText(true);

            String conditionText = "Potential Condition: " + response.potentialCondition;
            int conditionTextWidth = pageWidth - (2 * margin);
            StaticLayout conditionLayout = new StaticLayout(
                    conditionText,
                    conditionTextPaint,
                    conditionTextWidth,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0,
                    false
            );
            canvas.save();
            canvas.translate(x, y);
            conditionLayout.draw(canvas);
            canvas.restore();
            y += conditionLayout.getHeight() + 20;
        }


        canvas.drawText("Medications:", x, y, headerPaint);
        y += 25;
        normalPaint.setTextSize(16);
        for (PrescriptionResponse.Medication med : response.medications) {
            canvas.drawText("Name: " + med.name, x, y, normalPaint);
            y += 20;
            canvas.drawText("Dose: " + med.dose, x, y, normalPaint);
            y += 20;
            if (med.frequency != null && !med.frequency.isEmpty()) {
                canvas.drawText("Frequency: " + med.frequency, x, y, normalPaint);
                y += 20;
            }
            canvas.drawText("Duration: " + med.duration, x, y, normalPaint);
            y += 20;
            canvas.drawText("Side Effects: " + med.sideEffects, x, y, normalPaint);
            y += 30;
        }


        if ("Yes".equalsIgnoreCase(response.followUpRequired) &&
                response.questions != null && !response.questions.isEmpty()) {
            Paint questionPaint = new Paint();
            questionPaint.setColor(Color.MAGENTA);
            questionPaint.setTextSize(18);
            questionPaint.setFakeBoldText(true);
            canvas.drawText("Follow-Up Questions:", x, y, questionPaint);
            y += 25;
            questionPaint.setFakeBoldText(false);
            questionPaint.setTextSize(16);
            for (PrescriptionResponse.Question question : response.questions) {
                canvas.drawText(question.question, x, y, questionPaint);
                y += 20;
            }
        }


        pdfDocument.finishPage(page);

       String fileName = response.name + "_diagnosis_report.pdf";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/SmartMed");

        Uri pdfUri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);
        try {
            if (pdfUri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(pdfUri);
                pdfDocument.writeTo(outputStream);
                outputStream.close();
                Toast.makeText(this, "PDF saved: " + fileName, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error: PDF URI is null.", Toast.LENGTH_LONG).show();
            }
            pdfDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void processPrescriptionResponse(String jsonResponse) {
        try {
            jsonResponse = jsonResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            Gson gson = new Gson();
            PrescriptionResponse response = gson.fromJson(jsonResponse, PrescriptionResponse.class);
            if (response != null) {
                runOnUiThread(() -> {
                    outputTextView.setVisibility(View.VISIBLE);
                    loadingProgressBar.setVisibility(View.GONE);
                    StringBuilder outputBuilder = new StringBuilder();

                   if (response.name != null && !response.name.isEmpty()) {
                        outputBuilder.append("Name: ").append(response.name).append("\n");
                    }
                    if (response.age != null && !response.age.isEmpty()) {
                        outputBuilder.append("Age: ").append(response.age).append("\n");
                    }
                    outputBuilder.append("\n");
  outputBuilder.append("Prescription Summary: ")
                            .append(response.prescriptionSummary)
                            .append("\n\n");


                    if (response.potentialCondition != null && !response.potentialCondition.isEmpty()) {
                        outputBuilder.append("Potential Condition: ")
                                .append(response.potentialCondition)
                                .append("\n\n");
                    }

                   if (response.medications != null && !response.medications.isEmpty()) {
                        outputBuilder.append("Medications:\n");
                        for (PrescriptionResponse.Medication med : response.medications) {
                            outputBuilder.append("Name: ").append(med.name).append("\n")
                                    .append("Dose: ").append(med.dose).append("\n");
                            if (med.frequency != null && !med.frequency.isEmpty()) {
                                outputBuilder.append("Frequency: ").append(med.frequency).append("\n");
                            }
                            outputBuilder.append("Duration: ").append(med.duration).append("\n")
                                    .append("Side Effects: ").append(med.sideEffects).append("\n\n");
                        }
                    }
                    outputTextView.setText(outputBuilder.toString());
                   ImageView generatePdfButton = findViewById(R.id.btnDownloadPdf);
                   generatePdfButton.setVisibility(View.VISIBLE);
                    generatePdfButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            generatePdf(response);
                        }
                    });
  if ("Yes".equalsIgnoreCase(response.followUpRequired)
                            && response.questions != null && !response.questions.isEmpty()) {
                        showPrescriptionFollowUpQuestions(response.questions);
                    }
                });
            } else {
                Log.e(TAG, "Prescription response parsing returned null");
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Error processing prescription response: " + e.getMessage());
        }
    }

    private void showPrescriptionFollowUpQuestions(List<PrescriptionResponse.Question> questions) {
        LinearLayout followUpContainer = findViewById(R.id.followUpContainer);
        Button submitFollowUpButton = findViewById(R.id.submitFollowUpButton);

        followUpContainer.setVisibility(View.VISIBLE);
        followUpContainer.removeAllViews();

        TextView followUpTitle = new TextView(this);
        followUpTitle.setText("Prescription Follow-Up Questions:");
        followUpTitle.setTextSize(20);
        followUpTitle.setTextColor(getResources().getColor(R.color.colorAccent));
        followUpTitle.setPadding(0, 16, 0, 16);
        followUpContainer.addView(followUpTitle);

        for (PrescriptionResponse.Question question : questions) {
            TextView questionView = new TextView(this);
            questionView.setText(question.question);
            questionView.setTextSize(16);
            questionView.setTextColor(getResources().getColor(R.color.colorText));
            questionView.setTypeface(null, Typeface.BOLD);
            questionView.setPadding(0, 8, 0, 14);
            followUpContainer.addView(questionView);

            LinearLayout inputLayout = new LinearLayout(this);
            inputLayout.setOrientation(LinearLayout.HORIZONTAL);

            EditText answerInput = new EditText(this);
            answerInput.setTag(question.question);
            answerInput.setHint("Your answer...");
            answerInput.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            answerInput.setBackgroundResource(R.drawable.rounded_edittext);
            answerInput.setPadding(36, 18, 36, 18);
            LinearLayout.LayoutParams editTextLayoutParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            answerInput.setLayoutParams(editTextLayoutParams);

            ImageView voiceInputIcon = new ImageView(this);
            voiceInputIcon.setImageResource(R.drawable.baseline_mic_25);
            voiceInputIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            voiceInputIcon.setPadding(8, 8, 8, 8);
            voiceInputIcon.setOnClickListener(v -> startVoiceInput(answerInput));

            inputLayout.addView(answerInput);
            inputLayout.addView(voiceInputIcon);

            followUpContainer.addView(inputLayout);
        }

        submitFollowUpButton.setVisibility(View.VISIBLE);
        submitFollowUpButton.setOnClickListener(v -> submitPrescriptionFollowUpAnswers(questions));
    }

    private void submitPrescriptionFollowUpAnswers(List<PrescriptionResponse.Question> questions) {
      StringBuilder followUpConversation = new StringBuilder("Prescription Follow-Up:\n");
      LinearLayout followUpContainer = findViewById(R.id.followUpContainer);

      for (int i = 0; i < followUpContainer.getChildCount(); i++) {
            View child = followUpContainer.getChildAt(i);
            if (child instanceof TextView) {
                String questionText = ((TextView) child).getText().toString();
                followUpConversation.append("Question: \"").append(questionText).append("\"\n");
            }
            if (i + 1 < followUpContainer.getChildCount() && followUpContainer.getChildAt(i + 1) instanceof LinearLayout) {
                LinearLayout inputLayout = (LinearLayout) followUpContainer.getChildAt(i + 1);
                EditText answerInput = (EditText) inputLayout.getChildAt(0);
                String answerText = answerInput.getText().toString();
                followUpConversation.append("Answer: \"").append(answerText).append("\"\n");
                i++;
            }
        }
      Log.d(TAG, "Prescription follow-up conversation: " + followUpConversation.toString());
  }

    private class PrescriptionResponse {
        String name;
        String age;
        String prescriptionSummary;
        String potentialCondition;
        List<Medication> medications;
        String followUpRequired;
        List<Question> questions;

        private class Medication {
            String name;
            String dose;
            String frequency;
            String duration;
            String sideEffects;
        }

        private class Question {
            String question;
        }

    }




    private class ApiResponse {
        String followUpRequired;
        List<Question> questions;
        Diagnosis diagnosis;
        Advice advice;
        List<Medication> medications;
        FurtherTests furtherTests;
        String symptomAnalysis;

        private class Question {
            String question;
        }

        private class Diagnosis {
            String condition;
            String certainty;
        }

        private class Advice {
            String generalAdvice;
            String severity;
            String actionRequired;
            List<String> lifestyleChanges;
            String preventativeMeasures;
        }

        private class FurtherTests {
            String required;
            List<String> suggestedTests;
        }

        private class Medication {
            String name;
            String dose;
            String duration;
            List<String> sideEffects;
            String timingAdvice;

            public Medication(String name, String dose, List<String> sideEffects, String duration, String timingAdvice) {
                this.name = name;
                this.dose = dose;
                this.sideEffects = sideEffects;
                this.duration = duration;
                this.timingAdvice = timingAdvice;
            }
        }
    }


}
