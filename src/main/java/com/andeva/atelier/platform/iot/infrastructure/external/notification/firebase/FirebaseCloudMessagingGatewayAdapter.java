package com.andeva.atelier.platform.iot.infrastructure.external.notification.firebase;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.FcmNotificationAclPort;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Secondary adapter implementing {@link FcmNotificationAclPort} using Google Firebase Cloud Messaging (FCM).
 * Isolates Google Firebase Admin SDK and safely handles uninitialized Firebase configurations during local testing.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class FirebaseCloudMessagingGatewayAdapter implements FcmNotificationAclPort {

    private static final Logger log = LoggerFactory.getLogger(FirebaseCloudMessagingGatewayAdapter.class);

    private final CrmFleetAclPort crmFleetAclPort;

    public FirebaseCloudMessagingGatewayAdapter(CrmFleetAclPort crmFleetAclPort) {
        this.crmFleetAclPort = crmFleetAclPort;
    }

    @Override
    public String sendHighPriorityNotification(VehicleId vehicleId, String title, String body, Map<String, String> data) {
        String deviceToken = crmFleetAclPort.getDriverFcmDeviceToken(vehicleId);
        if (deviceToken == null || deviceToken.isBlank()) {
            log.warn("No FCM registration token found for vehicle {}", vehicleId.value());
            return null;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            String simulatedMessageId = "simulated-fcm-" + UUID.randomUUID();
            log.info("FirebaseApp not initialized in local environment. Simulated high-priority FCM notification {} dispatched to vehicle {}: [{}] {}",
                    simulatedMessageId, vehicleId.value(), title, body);
            return simulatedMessageId;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId("atelier_critical_alerts")
                                    .setSound("default")
                                    .build())
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("FCM push notification dispatched successfully for vehicle {}: {}", vehicleId.value(), response);
            return response;
        } catch (Exception e) {
            log.error("Failed to send FCM push notification for vehicle {}: {}", vehicleId.value(), e.getMessage());
            return null;
        }
    }
}
