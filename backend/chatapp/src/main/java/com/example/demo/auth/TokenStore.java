package com.example.demo.auth;

//package com.vrsec.chatapp.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {

    private final Map<String, String> tokenMap = new ConcurrentHashMap<>();

    public String createToken(String email) {
        String token = UUID.randomUUID().toString();
        tokenMap.put(token, email);
        return token;
    }

    public boolean isValid(String token) {
        return tokenMap.containsKey(token);
    }
}

