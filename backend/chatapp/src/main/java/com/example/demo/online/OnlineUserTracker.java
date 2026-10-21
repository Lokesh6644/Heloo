package com.example.demo.online;

import com.example.demo.matchmaking.MatchmakingService;
import org.springframework.stereotype.Component;

/**
 * Legacy adapter retained for compatibility with older callers.
 * WebSocketEventListener now tracks real WebSocket session IDs directly.
 */
@Component
public class OnlineUserTracker {
    private static final String LEGACY_SESSION_ID = "legacy-online-tracker";
    private final MatchmakingService matchmakingService;

    public OnlineUserTracker(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    public void userConnected(String email) {
        matchmakingService.userConnected(email, LEGACY_SESSION_ID);
    }

    public void userDisconnected(String email) {
        matchmakingService.userDisconnected(email, LEGACY_SESSION_ID);
    }
}