package com.andeva.atelier.platform.crm.interfaces.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a service appointment is scheduled.
 *
 * @param appointmentId            Identifier of the scheduled appointment
 * @param tenantId                 Identifier of the workshop tenant
 * @param branchId                 Identifier of the branch facility
 * @param customerId               Identifier of the requesting customer
 * @param vehicleId                Identifier of the vehicle
 * @param scheduledAt              Scheduled appointment timestamp
 * @param estimatedDurationMinutes Estimated duration in minutes
 * @param reason                   Reported reason or fault
 * @param occurredOn               Timestamp when the event occurred
 * @author Adiel Sanchez Santin
 */
public record AppointmentScheduledIntegrationEvent(
        UUID appointmentId,
        UUID tenantId,
        UUID branchId,
        UUID customerId,
        UUID vehicleId,
        Instant scheduledAt,
        int estimatedDurationMinutes,
        String reason,
        Instant occurredOn
) {
    public AppointmentScheduledIntegrationEvent {
        Objects.requireNonNull(appointmentId, "appointmentId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(branchId, "branchId cannot be null");
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(scheduledAt, "scheduledAt cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
