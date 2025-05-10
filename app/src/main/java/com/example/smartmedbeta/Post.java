package com.example.smartmedbeta;

import java.util.List;

public class Post {
    private String id;
    private String userId;
    private String title;
    private String content;
    private long timestamp;
    private int replyCount;
    private List<String> imageUrls;
    private String status;


    public Post() {}

    public Post(String id, String userId, String title, String content, long timestamp, int replyCount, List<String> imageUrls, String status) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.replyCount = replyCount;
        this.imageUrls = imageUrls;
        this.status = status;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
