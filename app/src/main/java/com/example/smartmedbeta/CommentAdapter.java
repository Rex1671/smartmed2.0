package com.example.smartmedbeta;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }


    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCommentContent, tvCommentType, tvCommentTime;

        public CommentViewHolder(View itemView) {
            super(itemView);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvCommentType = itemView.findViewById(R.id.tvCommentType);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.tvCommentContent.setText(comment.getContent());
        holder.tvCommentType.setText(comment.getType());


        String formattedTime = DateFormat.getDateTimeInstance().format(new Date(comment.getTimestamp()));
        holder.tvCommentTime.setText(formattedTime);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
}
