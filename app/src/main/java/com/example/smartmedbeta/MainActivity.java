package com.example.smartmedbeta;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.FractionRes;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.view.Window;

import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(MainActivity.class);
    private FirebaseAuth mAuth;
    private TextView logoutButton;
    private TextView welcomeTextView;
    private DatabaseReference databaseRef;
    private TextView chatMessageText;
    private ImageView floatingChatButton;
    private  ImageView chatButton;
private  TextView drugDiscovery;
private  ImageView profileImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        profileImage=findViewById(R.id.profileImage);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivty", "onClick: ");
                Intent intent = new Intent(MainActivity.this, ProfileSetupActivity.class);
                startActivity(intent);
            }
        });
        loadProfileImage();
        logoutButton = findViewById(R.id.logoutButton);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Log.d("MainActivity", "User is not logged in. Redirecting to SignupActivity...");
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
            return;
        }

        floatingChatButton = findViewById(R.id.floatingChatButton);
        drugDiscovery=findViewById(R.id.Drugdiscovery);
drugDiscovery.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, DrugDiscoveryActivity.class);
        startActivity(intent);
    }
});

        floatingChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });



//        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "User";
//        welcomeTextView.setText("Welcome, " + userEmail + "!");

        logoutButton.setOnClickListener(v -> logout());

        TextView symptomInputButton = findViewById(R.id.symptomInputButton);
        symptomInputButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SymptomInputActivity.class));
        });
        TextView  patientHistoryButton = findViewById(R.id.patientHistoryButton);
        patientHistoryButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PatientHistoryActivity.class));
        });
        TextView  patientListButton = findViewById(R.id.patientListButton);
        patientListButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        });

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
///////////////////////////////////////////

        chatMessageText = findViewById(R.id.chatMessageText);

        chatMessageText.setVisibility(View.INVISIBLE);


        new Handler().postDelayed(() -> {
            chatMessageText.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> chatMessageText.setVisibility(View.GONE), 5000);
        }, 1000);

        floatingChatButton.setOnLongClickListener(v -> {

            chatMessageText.setVisibility(View.VISIBLE);

              new Handler().postDelayed(() -> chatMessageText.setVisibility(View.GONE), 5000);

                return true;
        });


        //////////////////////////////////////////////////
        checkUserExists();
    }
    private void loadProfileImage() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String savedImageUrl = sharedPreferences.getString("profileImageUrl", null);

        if (savedImageUrl != null) {

            Glide.with(this)
                    .load(savedImageUrl)
                    .placeholder(R.drawable.boy)
                    .error(R.drawable.boy)
                    .apply(RequestOptions.circleCropTransform())

                    .into(profileImage);
        } else {

            profileImage.setImageResource(R.drawable.boy);
        }
    }


    private void checkUserExists() {
        Log.d("MainActivity", "Checking user existence...");
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId != null) {
            Log.d("MainActivity", "User ID: " + userId);


            databaseRef.child("patients").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.exists()) {
                        Log.d("MainActivity", "User data does not exist under patients node. Redirecting to ProfileSetupActivity...");
                        Intent intent = new Intent(MainActivity.this, ProfileSetupActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d("MainActivity", "User data exists under patients node.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("MainActivity", "Database error: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e("MainActivity", "User is not authenticated. Current user UID not found.");
        }
    }



    private void logout() {
        mAuth.signOut();
         finish();
    }
}
