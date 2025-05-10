package com.example.smartmedbeta;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;
import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class NewPostActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int MAX_WORD_COUNT = 1000;
    private static final int MAX_IMAGES = 4;

    private EditText etPostContent;
    private Button btnSelectImages, btnSubmitPost;
    private LinearLayout imagesContainer;

    private ArrayList<Uri> imageUris = new ArrayList<>();

    private StorageReference storageReference;
    private DatabaseReference postsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        etPostContent = findViewById(R.id.etPostContent);
        btnSelectImages = findViewById(R.id.btnSelectImages);
        btnSubmitPost = findViewById(R.id.btnSubmitPost);
        imagesContainer = findViewById(R.id.imagesContainer);

        storageReference = FirebaseStorage.getInstance().getReference("post_images");
        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        btnSelectImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImages();
            }
        });

        btnSubmitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { submitPost(); }
        });
    }
  private void selectImages() {
        if (imageUris.size() >= MAX_IMAGES) {
            Toast.makeText(this, "You can select up to " + MAX_IMAGES + " images.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count && imageUris.size() < MAX_IMAGES; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                    addImageToContainer(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                if (imageUris.size() < MAX_IMAGES) {
                    imageUris.add(imageUri);
                    addImageToContainer(imageUri);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addImageToContainer(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(imageUri);
        imagesContainer.addView(imageView);
    }


    private void submitPost() {
        String content = etPostContent.getText().toString().trim();
        int wordCount = content.isEmpty() ? 0 : content.split("\\s+").length;
        if (wordCount > MAX_WORD_COUNT) {
            Toast.makeText(this, "Post content exceeds the 1000 word limit.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (content.isEmpty() && imageUris.isEmpty()) {
            Toast.makeText(this, "Please add text or images.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Uploading post...", Toast.LENGTH_SHORT).show();
        uploadImagesAndSubmitPost(content);
    }

    private void uploadImagesAndSubmitPost(final String content) {
        if (imageUris.isEmpty()) {

            submitPostToDatabase(content, new ArrayList<String>());
        } else {
            final List<String> imageUrls = new ArrayList<>();
            final int totalImages = imageUris.size();
            final int[] uploadedCount = {0};

            for (Uri uri : imageUris) {

                final StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "_" + uri.getLastPathSegment());
                fileRef.putFile(uri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadUri) {
                                        imageUrls.add(downloadUri.toString());
                                        uploadedCount[0]++;
                                        if (uploadedCount[0] == totalImages) {

                                            submitPostToDatabase(content, imageUrls);
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(NewPostActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(NewPostActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private void submitPostToDatabase(String content, List<String> imageUrls) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String postId = postsRef.push().getKey();

        String userId = user.getUid();

        Post post = new Post();
        post.setUserId(userId);
        post.setId(postId);
        post.setTitle("");
        post.setContent(content);
        post.setImageUrls(imageUrls);
        post.setTimestamp(System.currentTimeMillis());
        post.setReplyCount(0);
        post.setStatus("pending");


        if (postId != null) {
            postsRef.child(postId).setValue(post)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(NewPostActivity.this, "Post created successfully", Toast.LENGTH_SHORT).show();

                         sendPostForModeration(postId, content);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(NewPostActivity.this, "Failed to create post", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void sendPostForModeration(String postId, String content) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("method", "moderate_post");
            JSONObject data = new JSONObject();
            data.put("post_id", postId);
            data.put("post_text", content);
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> showSnackbar("JSON Error: " + e.getMessage()));
            return;
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("https://67cb14c9bad56891a905.appwrite.global/")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showSnackbar("Moderation failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    boolean isOffensive = jsonResponse.optBoolean("is_offensive", false);
                    String reason = jsonResponse.optString("reason", "No specific reason provided.");

                    final String message = isOffensive
                            ? "❌ Post Rejected: " + reason
                            : "✅ Post Approved: Your content is clean.";

                    runOnUiThread(() -> showSnackbar(message));

                } catch (JSONException e) {
                    runOnUiThread(() -> showSnackbar("Parsing error: " + e.getMessage()));
                } finally {
                    response.close();
                }
            }
        });
    }


    private void showSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(5000);
        snackbar.show();
    }



}
