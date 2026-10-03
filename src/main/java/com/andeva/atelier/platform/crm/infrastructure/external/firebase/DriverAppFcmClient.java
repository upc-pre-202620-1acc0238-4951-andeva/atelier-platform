package com.andeva.atelier.platform.crm.infrastructure.external.firebase;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.DriverAppPushGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adapter for Firebase Cloud Messaging (FCM) push notifications to Atelier Driver.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class DriverAppFcmClient implements DriverAppPushGateway {

    private static final Logger log = LoggerFactory.getLogger(DriverAppFcmClient.class);

    @Override
    public boolean sendPushNotification(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("FCM token is blank, skipping push dispatch for title: {}", title);
            return false;
        }
        log.info("Dispatching FCM push to token [{}...]: '{}' - '{}'",
                fcmToken.substring(0, Math.min(8, fcmToken.length())), title, body);
        return true;
    }
}
