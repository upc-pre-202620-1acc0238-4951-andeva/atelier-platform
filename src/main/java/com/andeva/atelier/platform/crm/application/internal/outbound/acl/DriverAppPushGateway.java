package com.andeva.atelier.platform.crm.application.internal.outbound.acl;

import java.util.Map;

/**
 * Outbound ACL port for dispatching mobile push notifications to the Driver mobile application.
 *
 * @author Adiel Sanchez Santin
 */
public interface DriverAppPushGateway {

    boolean sendPushNotification(String fcmToken, String title, String body, Map<String, String> data);
}
