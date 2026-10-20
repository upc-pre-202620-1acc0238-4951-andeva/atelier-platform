package com.andeva.atelier.platform.shared.infrastructure.messaging.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sends a push notification to one device through Firebase Cloud Messaging. Without Firebase
 * credentials (local runs, tests) the push is only logged.
 */
@Component
public class FcmPushSender {

    private static final Logger log = LoggerFactory.getLogger(FcmPushSender.class);

    public boolean send(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            return false;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("[Simulated FCM] Push to token [{}...]: '{}' - '{}'",
                    fcmToken.substring(0, Math.min(8, fcmToken.length())), title, body);
            return true;
        }
        try {
            Message.Builder message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build());
            if (data != null && !data.isEmpty()) {
                message.putAllData(data);
            }
            FirebaseMessaging.getInstance().send(message.build());
            return true;
        } catch (Exception e) {
            log.error("Failed to send FCM push '{}': {}", title, e.getMessage());
            return false;
        }
    }
}
