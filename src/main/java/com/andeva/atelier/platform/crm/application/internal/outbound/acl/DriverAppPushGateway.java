package com.andeva.atelier.platform.crm.application.internal.outbound.acl;

import java.util.Map;
import java.util.UUID;

/**
 * Outbound ACL port for dispatching mobile push notifications to the Driver mobile application.
 *
 * @author Adiel Sanchez Santin
 */
public interface DriverAppPushGateway {

    boolean sendPushNotification(String fcmToken, String title, String body, Map<String, String> data);

    /**
     * Sends the notification to every device registered by the user accounts linked to the customer.
     *
     * @return number of devices the notification was dispatched to
     */
    int sendPushToCustomer(UUID customerId, String title, String body, Map<String, String> data);
}
