package com.andeva.atelier.platform.iot.infrastructure.external.notification.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Configuration responsible for safely initializing {@link FirebaseApp}
 * for Google Firebase Cloud Messaging (FCM) push notifications.
 * If credentials are not mounted, the system safely falls back to simulated push notifications.
 *
 * @author Joel Huamani Estefanero
 */
@Configuration
public class FirebaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfiguration.class);

    @Value("${firebase.credentials-path:firebase-service-account.json}")
    private String credentialsPath;

    @Value("${firebase.project-id:atelier-platform}")
    private String projectId;

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp already initialized.");
            return;
        }

        try {
            InputStream serviceAccount = null;
            File file = new File(credentialsPath);
            if (file.exists() && file.isFile()) {
                serviceAccount = new FileInputStream(file);
                log.info("Loading Firebase credentials from filesystem: {}", credentialsPath);
            } else {
                serviceAccount = getClass().getClassLoader().getResourceAsStream(credentialsPath);
                if (serviceAccount != null) {
                    log.info("Loading Firebase credentials from classpath: {}", credentialsPath);
                }
            }

            if (serviceAccount != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("FirebaseApp initialized successfully with project ID: {}", projectId);
            } else {
                log.warn("Firebase credentials file '{}' not found. FCM push notifications will operate in safe simulated mode.", credentialsPath);
            }
        } catch (Exception ex) {
            log.error("Failed to initialize FirebaseApp from {}: {}. FCM will operate in safe simulated mode.", credentialsPath, ex.getMessage());
        }
    }
}
