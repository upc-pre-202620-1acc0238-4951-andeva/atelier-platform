package com.andeva.atelier.platform.iot.interfaces.rest.resources.requests;

import java.time.Instant;
import java.util.UUID;

/**
 * Optional payload when converting a predictive DTC alert into a preventative appointment
 * using the branch, date/time, duration, and reason agreed with the customer.
 */
public record ConvertAlertToAppointmentRequest(
        UUID branchId,
        Instant scheduledAt,
        Integer estimatedDurationMinutes,
        String reason
) {
}
