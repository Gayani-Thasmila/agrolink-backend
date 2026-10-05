package com.agrolink.backend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class FirebaseMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseMessagingService.class);
    private static final String SERVICE_ACCOUNT_PATH = "firebase-service-account.json";

    private boolean initialized;

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource serviceAccount = new ClassPathResource(SERVICE_ACCOUNT_PATH);
            if (!serviceAccount.exists()) {
                logger.warn("Firebase service account file '{}' was not found. Notifications are disabled.",
                        SERVICE_ACCOUNT_PATH);
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            initialized = true;
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase messaging. Notifications are disabled.", e);
        }
    }

    public void sendNotification(String token, String title, String body) {
        if (token == null || token.isEmpty()) return;
        if (!initialized) {
            logger.debug("Skipping Firebase notification because Firebase messaging is not initialized.");
            return;
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Successfully sent Firebase message: {}", response);
        } catch (Exception e) {
            logger.error("Failed to send Firebase notification.", e);
        }
    }
}

