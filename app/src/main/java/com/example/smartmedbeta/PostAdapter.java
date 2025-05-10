package com.example.smartmedbeta;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Post post);
        void onCommentClick(Post post);
    }

    private List<Post> postList;
    private OnItemClickListener listener;

    public PostAdapter(List<Post> postList, OnItemClickListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    public void updateList(List<Post> newList) {
        postList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post, listener);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPostTitle, tvReplyCount;
        public Button btnAddComment;

        public PostViewHolder(View itemView) {
            super(itemView);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvReplyCount = itemView.findViewById(R.id.tvReplyCount);
            btnAddComment = itemView.findViewById(R.id.btnAddComment);
        }

        public void bind(final Post post, final OnItemClickListener listener) {
            Log.d("PostAdapter", "Binding post: " + post.getTitle());
            tvPostTitle.setText(post.getTitle());
            tvReplyCount.setText("Replies: " + post.getReplyCount());

          itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(post);
                }
            });

           btnAddComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCommentClick(post);
                }
            });
        }
    }
}
