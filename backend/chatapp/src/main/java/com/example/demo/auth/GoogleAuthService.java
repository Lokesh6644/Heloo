package com.example.demo.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Locale;

@Service
public class GoogleAuthService {
    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);

    @Value("${google.client.id}")
    private String clientId;

    public String verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                return null;
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();
            if (email == null || !Boolean.TRUE.equals(emailVerified)) {
                return null;
            }

            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            return normalizedEmail.endsWith("@vrsec.ac.in") ? normalizedEmail : null;
        } catch (Exception exception) {
            log.warn("Google ID token verification failed: {}", exception.getMessage());
            return null;
        }
    }
}