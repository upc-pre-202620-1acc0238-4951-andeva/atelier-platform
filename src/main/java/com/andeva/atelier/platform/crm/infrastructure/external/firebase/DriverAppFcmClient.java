package com.andeva.atelier.platform.crm.infrastructure.external.firebase;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.DriverAppPushGateway;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerMembershipRepository;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Firebase Cloud Messaging adapter delivering push notifications to the Driver mobile application.
 * Device tokens are those registered by the customer's linked user accounts in IAM; when Firebase
 * is not initialized (local development) dispatches are only logged.
 *
 * @author Adiel Sanchez Santin
 */
@Component
public class DriverAppFcmClient implements DriverAppPushGateway {

    private static final Logger log = LoggerFactory.getLogger(DriverAppFcmClient.class);

    private final CustomerMembershipRepository customerMembershipRepository;
    private final TenancyContextFacade tenancyContextFacade;

    public DriverAppFcmClient(
            CustomerMembershipRepository customerMembershipRepository,
            TenancyContextFacade tenancyContextFacade
    ) {
        this.customerMembershipRepository = Objects.requireNonNull(customerMembershipRepository, "CustomerMembershipRepository cannot be null");
        this.tenancyContextFacade = Objects.requireNonNull(tenancyContextFacade, "TenancyContextFacade cannot be null");
    }

    @Override
    public boolean sendPushNotification(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("FCM token is blank, skipping push dispatch for title: {}", title);
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

    @Override
    public int sendPushToCustomer(UUID customerId, String title, String body, Map<String, String> data) {
        if (customerId == null) {
            return 0;
        }
        return (int) customerMembershipRepository.findByCustomerId(CustomerId.of(customerId)).stream()
                .filter(membership -> membership.status() == CustomerMembershipStatus.ACTIVE)
                .map(membership -> tenancyContextFacade.fetchUserDeviceToken(membership.userId().value()))
                .flatMap(Optional::stream)
                .distinct()
                .filter(token -> sendPushNotification(token, title, body, data))
                .count();
    }
}
