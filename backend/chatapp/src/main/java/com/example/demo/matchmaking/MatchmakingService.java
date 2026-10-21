package com.example.demo.matchmaking;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MatchmakingService {
    private final Queue<String> waitingUsers = new ConcurrentLinkedQueue<>();
    private final Map<String, String> activeChats = new ConcurrentHashMap<>();
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    private final Map<String, Set<String>> sessionsByUser = new ConcurrentHashMap<>();

    public synchronized void userConnected(String email, String sessionId) {
        if (email == null || email.isBlank() || sessionId == null || sessionId.isBlank()) {
            return;
        }
        sessionsByUser.computeIfAbsent(email, key -> ConcurrentHashMap.newKeySet()).add(sessionId);
        onlineUsers.add(email);
    }

    public synchronized void userDisconnected(String email, String sessionId) {
        if (email == null || sessionId == null) {
            return;
        }

        Set<String> sessions = sessionsByUser.get(email);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (!sessions.isEmpty()) {
                return; // Another tab/device for this user is still connected.
            }
            sessionsByUser.remove(email);
        }

        onlineUsers.remove(email);
        waitingUsers.removeIf(email::equals);

        String partner = activeChats.remove(email);
        if (partner != null) {
            activeChats.remove(partner, email);
        }
    }

    public synchronized int getOnlineCount() {
        return onlineUsers.size();
    }

    public synchronized String findMatch(String email) {
        if (email == null || !onlineUsers.contains(email)) {
            return null;
        }
        if (activeChats.containsKey(email)) {
            return "MATCHED";
        }

        // Remove stale queue entries until a valid, available partner is found.
        String partner;
        while ((partner = waitingUsers.poll()) != null) {
            if (partner.equals(email)) {
                continue;
            }
            if (!onlineUsers.contains(partner) || activeChats.containsKey(partner)) {
                continue;
            }

            activeChats.put(email, partner);
            activeChats.put(partner, email);
            return "MATCHED";
        }

        if (!waitingUsers.contains(email)) {
            waitingUsers.add(email);
        }
        return null;
    }

    public synchronized String getPartner(String email) {
        return activeChats.get(email);
    }

    public synchronized void skip(String email) {
        if (email == null) {
            return;
        }

        String partner = activeChats.remove(email);
        if (partner != null) {
            activeChats.remove(partner, email);
            if (onlineUsers.contains(partner)
                    && !activeChats.containsKey(partner)
                    && !waitingUsers.contains(partner)) {
                waitingUsers.add(partner);
            }
        }

        waitingUsers.removeIf(email::equals);
    }
}