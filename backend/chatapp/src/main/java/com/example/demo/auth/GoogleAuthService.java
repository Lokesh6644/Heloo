package com.example.demo.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    private final String CLIENT_ID = "742934103427-fblv3mdlhhfk6lu91q6pis8pmbnaoq24.apps.googleusercontent.com";

    public String verifyToken(String token) {

        try {

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);

            if (idToken != null) {

                GoogleIdToken.Payload payload = idToken.getPayload();
                return payload.getEmail();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}