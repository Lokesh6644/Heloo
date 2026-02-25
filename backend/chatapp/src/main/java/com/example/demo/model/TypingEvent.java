package com.example.demo.model;

public class TypingEvent {

    private boolean typing;

    public TypingEvent() {}

    public TypingEvent(boolean typing) {
        this.typing = typing;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }
}