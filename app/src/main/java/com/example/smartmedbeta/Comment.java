package com.example.smartmedbeta;


public class Comment {
    private String id;
    private String content;
    private String type; // e.g., "user", "moderator", "ai"
    private long timestamp;

    // Default constructor required for Firebase
    public Comment() {
    }

    public Comment(String id, String content, String type, long timestamp) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

