package com.example.demo.matchmaking;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MatchmakingService {
    private final Queue<String> waitingUsers = new ConcurrentLinkedQueue<>();
    private final Map<String, String> activeChats = new ConcurrentHashMap<>();
    private final Map<String, String> lastPartners = new ConcurrentHashMap<>();
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    private final Map<String, Set<String>> sessionsByUser = new ConcurrentHashMap<>();

    public synchronized void userConnected(String email, String sessionId) {
        if (email == null || email.isBlank() || sessionId == null || sessionId.isBlank()) return;
        sessionsByUser.computeIfAbsent(email, key -> ConcurrentHashMap.newKeySet()).add(sessionId);
        onlineUsers.add(email);
    }

    /**
     * @return true only when this was the user's final connected session.
     */
    public synchronized boolean userDisconnected(String email, String sessionId) {
        if (email == null || sessionId == null) return false;

        Set<String> sessions = sessionsByUser.get(email);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (!sessions.isEmpty()) return false;
            sessionsByUser.remove(email);
        }

        onlineUsers.remove(email);
        waitingUsers.removeIf(email::equals);

        String partner = activeChats.remove(email);
        if (partner != null) {
            activeChats.remove(partner, email);
        }
        return true;
    }

    public synchronized int getOnlineCount() {
        return onlineUsers.size();
    }

    public synchronized String findMatch(String email) {
        if (email == null || !onlineUsers.contains(email)) return null;
        if (activeChats.containsKey(email)) return "MATCHED";

        // Prefer a new partner, but do not leave two available users waiting forever
        // when their only possible partner is the person they most recently skipped.
        int candidatesToInspect = waitingUsers.size();
        List<String> deferred = new ArrayList<>();
        String lastPartnerCandidate = null;

        for (int i = 0; i < candidatesToInspect; i++) {
            String partner = waitingUsers.poll();
            if (partner == null) break;
            if (partner.equals(email)) continue;
            if (!onlineUsers.contains(partner) || activeChats.containsKey(partner)) continue;

            if (partner.equals(lastPartners.get(email))) {
                if (lastPartnerCandidate == null) lastPartnerCandidate = partner;
                else deferred.add(partner);
                continue;
            }

            activeChats.put(email, partner);
            activeChats.put(partner, email);
            lastPartners.remove(email);
            lastPartners.remove(partner);
            waitingUsers.addAll(deferred);
            if (lastPartnerCandidate != null) waitingUsers.add(lastPartnerCandidate);
            return "MATCHED";
        }

        // No alternative partner was available; pair with the last partner as a fallback.
        if (lastPartnerCandidate != null
                && onlineUsers.contains(lastPartnerCandidate)
                && !activeChats.containsKey(lastPartnerCandidate)) {
            activeChats.put(email, lastPartnerCandidate);
            activeChats.put(lastPartnerCandidate, email);
            lastPartners.remove(email);
            lastPartners.remove(lastPartnerCandidate);
            waitingUsers.addAll(deferred);
            return "MATCHED";
        }

        waitingUsers.addAll(deferred);
        if (lastPartnerCandidate != null) waitingUsers.add(lastPartnerCandidate);
        if (!waitingUsers.contains(email)) waitingUsers.add(email);
        return null;
    }

    public synchronized String getPartner(String email) {
        return activeChats.get(email);
    }

    public synchronized void skip(String email) {
        if (email == null) return;

        String partner = activeChats.remove(email);
        if (partner != null) {
            activeChats.remove(partner, email);
            lastPartners.put(email, partner);
            lastPartners.put(partner, email);
            // The partner is re-queued when their /user/topic/left event arrives.
        }
        waitingUsers.removeIf(email::equals);
    }
}