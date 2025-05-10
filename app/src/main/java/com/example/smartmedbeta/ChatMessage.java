package com.example.smartmedbeta;
public class ChatMessage {
    private String role;
    private String text;

    public ChatMessage(String role, String text) {
        this.role = role;
        this.text = text;
    }

    public String getRole() {
        return role;
    }

    public String getText() {
        return text;
    }
}
