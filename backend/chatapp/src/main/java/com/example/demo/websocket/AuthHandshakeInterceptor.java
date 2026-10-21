package com.example.demo.websocket;

import com.example.demo.auth.GoogleAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
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
        List<String> tokens = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .get("token");

        if (tokens == null || tokens.size() != 1 || tokens.get(0).isBlank()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String email = googleAuthService.verifyToken(tokens.get(0));
        if (email == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

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
        // No post-handshake action required.
    }
}