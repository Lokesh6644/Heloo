package com.example.demo.websocket;

import com.example.demo.matchmaking.MatchmakingService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class WebSocketEventListener {
    private final MatchmakingService matchmakingService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(MatchmakingService matchmakingService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.matchmakingService = matchmakingService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() == null || accessor.getSessionId() == null) {
            return;
        }
        matchmakingService.userConnected(accessor.getUser().getName(), accessor.getSessionId());
        broadcastOnlineCount();
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() == null || accessor.getSessionId() == null) {
            return;
        }
        matchmakingService.userDisconnected(accessor.getUser().getName(), accessor.getSessionId());
        broadcastOnlineCount();
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if ("/topic/onlineCount".equals(accessor.getDestination()) && accessor.getUser() != null) {
            messagingTemplate.convertAndSendToUser(
                    accessor.getUser().getName(),
                    "/topic/onlineCount",
                    matchmakingService.getOnlineCount()
            );
        }
    }

    private void broadcastOnlineCount() {
        messagingTemplate.convertAndSend("/topic/onlineCount", matchmakingService.getOnlineCount());
    }
}