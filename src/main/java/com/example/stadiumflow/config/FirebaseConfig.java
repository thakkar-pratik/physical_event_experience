package com.example.stadiumflow.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Firebase Configuration for Analytics and Cloud Messaging.
 * Only active in production or when explicitly enabled.
 */
@Configuration
@Profile({"prod", "firebase"})
public class FirebaseConfig {
    
    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);
    
    @Value("${spring.cloud.gcp.project-id}")
    private String projectId;
    
    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setProjectId(projectId)
                    .build();
                
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase initialized successfully for project: {}", projectId);
            } else {
                log.info("✅ Firebase already initialized");
            }
        } catch (IOException e) {
            log.warn("⚠️ Firebase initialization failed (will continue without Firebase): {}", e.getMessage());
        }
    }
}
