package com.example.demo.matchmaking;

import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class MatchmakingService {

    private final Queue<String> waitingUsers = new ConcurrentLinkedQueue<>();
    private final Map<String, String> activeChats = new ConcurrentHashMap<>();

    public synchronized String findMatch(String sessionId) {

        if (!waitingUsers.isEmpty()) {
            String partner = waitingUsers.poll();

            if (partner != null && !partner.equals(sessionId)) {
                String chatId = UUID.randomUUID().toString();

                activeChats.put(sessionId, partner);
                activeChats.put(partner, sessionId);

                return chatId;
            }
        }

        waitingUsers.add(sessionId);
        return null;
    }

    public String getPartner(String sessionId) {
        return activeChats.get(sessionId);
    }

    public synchronized void skip(String sessionId) {

        String partner = activeChats.remove(sessionId);

        if (partner != null) {
            activeChats.remove(partner);
        }

        waitingUsers.remove(sessionId);
    }

    public synchronized String disconnectUser(String sessionId) {

        waitingUsers.remove(sessionId);

        String partner = activeChats.remove(sessionId);

        if (partner != null) {
            activeChats.remove(partner);
            waitingUsers.add(partner);
        }

        return partner;
    }
}