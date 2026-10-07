package com.andeva.atelier.platform.iot.application.internal.outbound.acl;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.util.Map;

/**
 * Outbound Anti-Corruption Layer port for dispatching push notifications via Firebase Cloud Messaging.
 *
 * @author Joel Huamani Estefanero
 */
public interface FcmNotificationAclPort {

    /**
     * Sends a high-priority push notification to the driver and workshop personnel.
     *
     * @param vehicleId target vehicle identifier
     * @param title     notification title
     * @param body      notification body message
     * @param data      additional payload key-value metadata
     * @return FCM message receipt identifier or dispatch status
     */
    String sendHighPriorityNotification(
            VehicleId vehicleId,
            String title,
            String body,
            Map<String, String> data
    );
}
