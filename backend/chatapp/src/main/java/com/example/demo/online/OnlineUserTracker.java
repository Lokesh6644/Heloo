//package com.example.demo.online;
//
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class OnlineUserTracker {
//
//    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public OnlineUserTracker(SimpMessagingTemplate messagingTemplate) {
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    public void userConnected(String username) {
//        onlineUsers.add(username);
//        broadcast();
//    }
//
//    public void userDisconnected(String username) {
//        onlineUsers.remove(username);
//        broadcast();
//    }
//
//    private void broadcast() {
//        messagingTemplate.convertAndSend("/topic/onlineCount", onlineUsers.size());
//    }
//}



package com.example.demo.online;

import com.example.demo.matchmaking.MatchmakingService;
import org.springframework.stereotype.Component;

@Component
public class OnlineUserTracker {

    private final MatchmakingService matchmakingService;

    public OnlineUserTracker(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    public void userConnected(String email) {
        System.out.println("📱 OnlineUserTracker: Adding " + email + " to online users");
        matchmakingService.userConnected(email);
    }

    public void userDisconnected(String email) {
        System.out.println("📱 OnlineUserTracker: Removing " + email + " from online users");
        matchmakingService.userDisconnected(email);
    }
}