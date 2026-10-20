package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.util.Map;
import java.util.UUID;

/**
 * Outbound port for push notifications to workshop staff devices.
 */
public interface StaffNotificationPort {

    /**
     * Notifies the person behind a staff membership (e.g. the mechanic assigned to a task).
     */
    void notifyMember(UUID membershipId, String title, String body, Map<String, String> data);

    /**
     * Notifies every active staff member of the tenant holding the permission (e.g. supervisors).
     */
    void notifyPermissionHolders(UUID tenantId, String permission, String title, String body, Map<String, String> data);
}
