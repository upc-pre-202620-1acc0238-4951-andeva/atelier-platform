package com.andeva.atelier.platform.hr.application.internal.outbound.acl;

import java.util.Map;
import java.util.UUID;

/**
 * Outbound port for push notifications to an employee's device.
 */
public interface EmployeeNotificationPort {

    void notifyEmployee(UUID membershipId, String title, String body, Map<String, String> data);
}
