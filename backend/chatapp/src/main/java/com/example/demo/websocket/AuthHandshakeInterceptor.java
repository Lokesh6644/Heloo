package com.example.demo.websocket;

import com.example.demo.auth.GoogleAuthService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final GoogleAuthService googleAuthService;

    public AuthHandshakeInterceptor(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        String query = request.getURI().getQuery(); // token=xxxxx

        if (query == null || !query.startsWith("token=")) {
            return false;
        }

        // Extract token
        String token = query.substring(6);

        // Verify Google token
        String email = googleAuthService.verifyToken(token);

        // Allow only VRSEC domain
        if (email == null || !email.endsWith("@vrsec.ac.in")) {
            return false;
        }

        // Store authenticated user
        attributes.put("user", email);

        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // Nothing required
    }
}