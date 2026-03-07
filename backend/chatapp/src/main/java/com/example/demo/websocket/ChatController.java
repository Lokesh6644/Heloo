
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

    @MessageMapping("/join")
    public void joinChat(Principal principal) {

        String email = principal.getName();

        String chatId = matchmakingService.findMatch(email);

        if (chatId != null) {

            String partner = matchmakingService.getPartner(email);

            if (partner != null) {

                // Notify both users
                messagingTemplate.convertAndSendToUser(email, "/topic/match", "matched");
                messagingTemplate.convertAndSendToUser(partner, "/topic/match", "matched");

                // Send again after slight delay to avoid missed subscription
                new Thread(() -> {
                    try { Thread.sleep(300); } catch (Exception ignored) {}

                    messagingTemplate.convertAndSendToUser(email, "/topic/match", "matched");
                    messagingTemplate.convertAndSendToUser(partner, "/topic/match", "matched");

                }).start();
            }
        }
    }


    @MessageMapping("/getOnlineCount")
    public void getOnlineCount(Principal principal) {
        int count = matchmakingService.getOnlineCount();
        System.out.println("Sending online count to " + principal.getName() + ": " + count);
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/topic/onlineCount",
                count
        );
    }







    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessage message,
                            Principal principal) {

        String email = principal.getName();
        String partner = matchmakingService.getPartner(email);

        if (partner != null) {
            messagingTemplate.convertAndSendToUser(
                    partner,
                    "/topic/messages",
                    message
            );
            System.out.println("Message from " + email + " to " + partner);
        }
    }

    @MessageMapping("/typing")
    public void typingEvent(@Payload TypingEvent event,
                            Principal principal) {

        String email = principal.getName();
        String partner = matchmakingService.getPartner(email);

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

        String email = principal.getName();
        String partner = matchmakingService.getPartner(email);

        matchmakingService.skip(email);

        if (partner != null) {
            messagingTemplate.convertAndSendToUser(
                    partner,
                    "/topic/left",
                    "Stranger disconnected"
            );
        }

        joinChat(principal);
    }

}