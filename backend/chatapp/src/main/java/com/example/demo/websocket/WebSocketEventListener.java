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
        if (accessor.getUser() != null) {
            String email = accessor.getUser().getName();
            matchmakingService.userConnected(email);
            System.out.println("CONNECT: " + email);

            // Broadcast updated count to ALL users
            broadcastOnlineCount();
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String email = accessor.getUser().getName();
            matchmakingService.userDisconnected(email);
            System.out.println("DISCONNECT: " + email);

            // Broadcast updated count to ALL users
            broadcastOnlineCount();
        }
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        // When someone subscribes to onlineCount, send them the current count immediately
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if ("/topic/onlineCount".equals(destination)) {
            // Send current count to this specific user
            messagingTemplate.convertAndSendToUser(
                    accessor.getUser().getName(),
                    "/topic/onlineCount",
                    matchmakingService.getOnlineCount()
            );
        }
    }

    private void broadcastOnlineCount() {
        int count = matchmakingService.getOnlineCount();
        System.out.println("Broadcasting online count: " + count);
        messagingTemplate.convertAndSend("/topic/onlineCount", count);
    }
}