package com.example.smartmedbeta;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.FutureCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText editTextUserMessage;
    private ProgressBar progressBar;
    private static final String TAG = "ChatActivity";
    private static final String API_KEY = "AIzaSyDTMqspY_C4KUwAqozRE7STorg0-HZD2yU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextUserMessage = findViewById(R.id.editTextUserMessage);
        progressBar = findViewById(R.id.progressBar);

        chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage("AI", "Hello! I'm your virtual medical advisor. How can I assist you today?"));
        chatMessages.add(new ChatMessage("AI", "Feel free to ask me any health-related questions you may have."));
        chatMessages.add(new ChatMessage("AI", "Please note that I'm not a doctor, but I can provide general advice."));

        chatAdapter = new ChatAdapter(chatMessages);
        recyclerViewChat.setAdapter(chatAdapter);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));


        findViewById(R.id.buttonSend).setOnClickListener(v -> sendMessage());

        ImageView fabAddPost = findViewById(R.id.buttonlive);
        fabAddPost.setOnClickListener(v -> {

            Intent intent = new Intent(ChatActivity.this, LiveChatActivity.class);
            startActivity(intent);
        });
    }

    private void sendMessage() {
        String userMessage = editTextUserMessage.getText().toString().trim();
        if (!userMessage.isEmpty()) {

            chatMessages.add(new ChatMessage("User", userMessage));
            chatAdapter.notifyDataSetChanged();
            recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
            editTextUserMessage.setText("");

            sendQueryToGemini(userMessage);

        }
    }

    private void sendQueryToGemini(String userQuery) {

        progressBar.setVisibility(View.VISIBLE);


        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Act as a medical advisor. Answer the following questions with appropriate medical advice. and write as plain text dont use bold \n");
        promptBuilder.append("Previous Conversation:\n");

        for (ChatMessage message : chatMessages) {
            promptBuilder.append(message.getRole()).append(": ").append(message.getText()).append("\n");
        }

        promptBuilder.append("User: ").append(userQuery).append("\n");
        String prompt = promptBuilder.toString();
        askGemini(prompt);
    }



    private void askGemini(String prompt) {

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder().addText(prompt).build();
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {

                runOnUiThread(() -> {
                    String resultText = result.getText();
                    Log.d(TAG, "Api Response: "+resultText);
                    onGeminiResponse(resultText);
                });
            }

            @Override
            public void onFailure(Throwable t) {

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error: " + t.getMessage());
                    Toast.makeText(ChatActivity.this, "Error communicating with the server", Toast.LENGTH_SHORT).show();
                });
            }
        }, executor);
    }

    private void onGeminiResponse(String response) {
        progressBar.setVisibility(View.GONE);
        response = response.trim();


        chatMessages.add(new ChatMessage("AI", response));
        chatAdapter.notifyDataSetChanged();
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
    }


    private boolean isJsonValid(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }
}
