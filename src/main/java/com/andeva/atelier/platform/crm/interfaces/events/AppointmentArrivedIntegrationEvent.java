package com.andeva.atelier.platform.crm.interfaces.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a vehicle arrives at the workshop for its appointment.
 *
 * @param appointmentId Identifier of the appointment
 * @param tenantId      Identifier of the workshop tenant
 * @param branchId      Identifier of the branch facility
 * @param customerId    Identifier of the customer
 * @param vehicleId     Identifier of the vehicle
 * @param occurredOn    Timestamp when the vehicle arrived
 * @author Adiel Sanchez Santin
 */
public record AppointmentArrivedIntegrationEvent(
        UUID appointmentId,
        UUID tenantId,
        UUID branchId,
        UUID customerId,
        UUID vehicleId,
        Instant occurredOn
) {
    public AppointmentArrivedIntegrationEvent {
        Objects.requireNonNull(appointmentId, "appointmentId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(branchId, "branchId cannot be null");
        Objects.requireNonNull(customerId, "customerId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
