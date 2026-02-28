package com.example.demo.matchmaking;

import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;

@Service
public class MatchmakingService {

    private final Queue<String> waitingUsers = new ConcurrentLinkedQueue<>();
    private final Map<String, String> activeChats = new ConcurrentHashMap<>();
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void userConnected(String email) {
        onlineUsers.add(email);
    }

    public void userDisconnected(String email) {
        onlineUsers.remove(email);
    }

    public int getOnlineCount() {
        return onlineUsers.size();
    }

    public synchronized String findMatch(String email) {

        if (activeChats.containsKey(email)) {
            return null;
        }

        while (!waitingUsers.isEmpty()) {
            String partner = waitingUsers.poll();

            if (partner != null && !partner.equals(email)) {

                activeChats.put(email, partner);
                activeChats.put(partner, email);

                return UUID.randomUUID().toString();
            }
        }

        if (!waitingUsers.contains(email)) {
            waitingUsers.add(email);
        }

        return null;
    }

    public String getPartner(String email) {
        return activeChats.get(email);
    }

    public synchronized void skip(String email) {

        String partner = activeChats.remove(email);

        if (partner != null) {
            activeChats.remove(partner);
            waitingUsers.add(partner);
        }

        waitingUsers.remove(email);
    }

    public synchronized String disconnectUser(String email) {

        waitingUsers.remove(email);

        String partner = activeChats.remove(email);

        if (partner != null) {
            activeChats.remove(partner);
            waitingUsers.add(partner);
        }

        return partner;
    }
}