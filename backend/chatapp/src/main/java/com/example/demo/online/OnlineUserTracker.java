package com.example.demo.online;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserTracker {

    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    private final SimpMessagingTemplate messagingTemplate;

    public OnlineUserTracker(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void userConnected(String username) {
        onlineUsers.add(username);
        broadcast();
    }

    public void userDisconnected(String username) {
        onlineUsers.remove(username);
        broadcast();
    }

    private void broadcast() {
        messagingTemplate.convertAndSend("/topic/onlineCount", onlineUsers.size());
    }
}