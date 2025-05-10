package com.example.smartmedbeta;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> allPosts = new ArrayList<>();
    private SearchView searchView;
    private DatabaseReference postsRef;
    private FloatingActionButton fabAddPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        searchView = findViewById(R.id.searchView);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));

        fabAddPost = findViewById(R.id.fabAddPost);
        fabAddPost.setOnClickListener(v -> {

            Intent intent = new Intent(DashboardActivity.this, NewPostActivity.class);
            startActivity(intent);
        });

       postAdapter = new PostAdapter(allPosts, new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Post post) {
                Intent intent = new Intent(DashboardActivity.this, PostDetailActivity.class);
                intent.putExtra("postId", post.getId());
                startActivity(intent);
            }

            @Override
            public void onCommentClick(Post post) {
                   showAddCommentDialog(post);
            }
        });
        recyclerViewPosts.setAdapter(postAdapter);


        postsRef = FirebaseDatabase.getInstance().getReference("posts");

      loadPosts();


        setupSearch();
    }


    private void loadPosts() {
        Query query = postsRef.orderByChild("status"); // Filter by 'status'

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allPosts.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        post.setId(postSnapshot.getKey());
                        allPosts.add(post);
                    }
                }

                Log.d("DashboardActivity", "Verified Posts count: " + allPosts.size());
                Collections.reverse(allPosts);
                postAdapter.updateList(allPosts);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPosts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    postAdapter.updateList(allPosts);
                } else {
                    filterPosts(newText);
                }
                return true;
            }
        });
    }

    private void filterPosts(String query) {
        List<Post> filteredList = new ArrayList<>();
        for (Post post : allPosts) {
            if (post.getTitle() != null && post.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(post);
            }
        }
        postAdapter.updateList(filteredList);
    }

    private void showAddCommentDialog(final Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Comment");

        final EditText input = new EditText(this);
        input.setHint("Write your comment...");
        builder.setView(input);

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String commentText = input.getText().toString().trim();
                if (!commentText.isEmpty()) {
                    addCommentToPost(post, commentText);
                } else {
                    Toast.makeText(DashboardActivity.this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addCommentToPost(final Post post, String commentText) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(post.getId());
        String commentId = commentsRef.push().getKey();
        Comment comment = new Comment(commentId, commentText, "user", System.currentTimeMillis());
        if (commentId != null) {
            commentsRef.child(commentId).setValue(comment)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(DashboardActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                            updateReplyCount(post);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(DashboardActivity.this, "Failed to add comment", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

      private void updateReplyCount(final Post post) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.getId());
        postRef.child("replyCount").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long currentCount = mutableData.getValue(Long.class);
                if (currentCount == null) {
                    currentCount = 0L;
                }
                mutableData.setValue(currentCount + 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
              }
        });
    }
}
