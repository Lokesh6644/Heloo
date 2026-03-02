package com.example.demo.websocket;

import com.example.demo.online.OnlineUserTracker;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Date;

@Component
public class WebSocketEventListener {

    private final OnlineUserTracker tracker;

    public WebSocketEventListener(OnlineUserTracker tracker) {
        this.tracker = tracker;
    }

//    @EventListener
//    public void handleConnect(SessionConnectEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        if (accessor.getUser() != null) {
//            tracker.userConnected(accessor.getUser().getName());
//        }
//    }
//
//    @EventListener
//    public void handleDisconnect(SessionDisconnectEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        if (accessor.getUser() != null) {
//            tracker.userDisconnected(accessor.getUser().getName());
//        }
//    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String email = accessor.getUser().getName();
            tracker.userConnected(email);
            System.out.println("CONNECT: " + email + " at " + new Date());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String email = accessor.getUser().getName();
            tracker.userDisconnected(email);
            System.out.println("DISCONNECT: " + email + " at " + new Date());
        }
    }


}