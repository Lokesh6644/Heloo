package com.example.demo.websocket;

import com.example.demo.matchmaking.MatchmakingService;
import com.example.demo.model.ChatMessage;
import com.example.demo.model.TypingEvent;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;




@Controller
public class ChatController {

    private final MatchmakingService matchmakingService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(MatchmakingService matchmakingService,
                          SimpMessagingTemplate messagingTemplate) {
        this.matchmakingService = matchmakingService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessage message,
                            Principal principal) {

        String sender = principal.getName();
        String partner = matchmakingService.getPartner(sender);

        if (partner != null) {

            messagingTemplate.convertAndSendToUser(
                    partner,
                    "/topic/messages",
                    message
            );
        }
    }

    @MessageMapping("/join")
    public void joinChat(Principal principal) {

        String sessionId = principal.getName();
        String chatId = matchmakingService.findMatch(sessionId);

        if (chatId != null) {

            String partner = matchmakingService.getPartner(sessionId);

            // Notify current user
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/topic/match",
                    "matched"
            );

            // Notify partner user
            messagingTemplate.convertAndSendToUser(
                    partner,
                    "/topic/match",
                    "matched"
            );
        }
    }


    @MessageMapping("/typing")
    public void typingEvent(@Payload TypingEvent event,
                            Principal principal) {

        String sender = principal.getName();
        String partner = matchmakingService.getPartner(sender);

        if (partner != null) {
            messagingTemplate.convertAndSendToUser(
                    partner,
                    "/topic/typing",
                    event
            );
        }
    }

    @MessageMapping("/next")
    public void nextUser(Principal principal) {

        String sessionId = principal.getName();
        String partner = matchmakingService.getPartner(sessionId);

        matchmakingService.skip(sessionId);

        // Notify partner that stranger left
        if (partner != null) {
            messagingTemplate.convertAndSendToUser(
                    partner,
                    "/topic/left",
                    "Stranger disconnected"
            );
        }

        // Rejoin queue
        joinChat(principal);
    }


}