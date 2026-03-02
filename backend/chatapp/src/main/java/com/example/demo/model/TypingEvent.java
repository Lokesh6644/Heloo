package com.example.demo.model;

public class TypingEvent {
    private boolean typing;
    private String timestamp;

    // Default constructor (required for JSON)
    public TypingEvent() {}

    public TypingEvent(boolean typing) {
        this.typing = typing;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    // Getters and setters
    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}