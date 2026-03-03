package com.example.demo.matchmaking;

import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;

//@Service
//public class MatchmakingService {
//
//    private final Queue<String> waitingUsers = new ConcurrentLinkedQueue<>();
//    private final Map<String, String> activeChats = new ConcurrentHashMap<>();
//    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
//
//    public void userConnected(String email) {
//        onlineUsers.add(email);
//    }
//
//    public void userDisconnected(String email) {
//        onlineUsers.remove(email);
//    }
//
//    public int getOnlineCount() {
//        return onlineUsers.size();
//    }
//
//    public synchronized String findMatch(String email) {
//
//        if (activeChats.containsKey(email)) {
//            return null;
//        }
//
//        while (!waitingUsers.isEmpty()) {
//            String partner = waitingUsers.poll();
//
//            if (partner != null && !partner.equals(email)) {
//
//                activeChats.put(email, partner);
//                activeChats.put(partner, email);
//
//                return UUID.randomUUID().toString();
//            }
//        }
//
//        if (!waitingUsers.contains(email)) {
//            waitingUsers.add(email);
//        }
//
//        return null;
//    }
//
//    public String getPartner(String email) {
//        return activeChats.get(email);
//    }
//
//    public synchronized void skip(String email) {
//
//        String partner = activeChats.remove(email);
//
//        if (partner != null) {
//            activeChats.remove(partner);
//            waitingUsers.add(partner);
//        }
//
//        waitingUsers.remove(email);
//    }
//
//    public synchronized String disconnectUser(String email) {
//
//        waitingUsers.remove(email);
//
//        String partner = activeChats.remove(email);
//
//        if (partner != null) {
//            activeChats.remove(partner);
//            waitingUsers.add(partner);
//        }
//
//        return partner;
//    }
//}


@Service
public class MatchmakingService {

    private final Queue<String> waitingUsers = new ConcurrentLinkedQueue<>();
    private final Map<String, String> activeChats = new ConcurrentHashMap<>();
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    private final Object matchLock = new Object(); // Add lock for matching

    public void userConnected(String email) {
        onlineUsers.add(email);
        System.out.println("User connected: " + email + " | Online: " + onlineUsers.size());
    }

    public void userDisconnected(String email) {
        onlineUsers.remove(email);
        waitingUsers.remove(email);

        // Handle active chat disconnection
        String partner = activeChats.remove(email);
        if (partner != null) {
            activeChats.remove(partner);
            // Don't auto-add to waiting - let them reconnect manually
        }
        System.out.println("User disconnected: " + email + " | Online: " + onlineUsers.size());
    }

    public int getOnlineCount() {
        return onlineUsers.size();
    }

    public synchronized String findMatch(String email) {
        System.out.println("findMatch called for: " + email);
        System.out.println("Waiting queue size: " + waitingUsers.size());
        System.out.println("Online users: " + onlineUsers.size());

        // Already in chat?
        if (activeChats.containsKey(email)) {
            System.out.println(email + " already in chat with: " + activeChats.get(email));
            return "MATCHED";
        }

        // Look for waiting user
        String partner = waitingUsers.poll();

        if (partner != null && !partner.equals(email) && onlineUsers.contains(partner) ) {
            // Create match
            activeChats.put(email, partner);
            activeChats.put(partner, email);

            System.out.println("✅ MATCH CREATED: " + email + " <-> " + partner);
            return "MATCHED";
        }

        // If partner was invalid, put them back? No, they're removed from queue
        if (partner != null) {
            System.out.println("Invalid partner: " + partner + " - not adding back to queue");
        }

        // No match, add to waiting
        if (!waitingUsers.contains(email)) {
            waitingUsers.add(email);
            System.out.println("✅ " + email + " added to waiting queue. Queue size: "  + waitingUsers.size() );
        }

        return null;
    }

    public String getPartner(String email) {
        return activeChats.get(email); // Returns email for backend use only
    }

    public void skip(String email) {
        synchronized(matchLock) {
            String partner = activeChats.remove(email);
            if (partner != null) {
                activeChats.remove(partner);
                // Add partner back to waiting if still online
                if (onlineUsers.contains(partner)) {
                    waitingUsers.add(partner);
                }
            }
            waitingUsers.remove(email);
        }
    }
}