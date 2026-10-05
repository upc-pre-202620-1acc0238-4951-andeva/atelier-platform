package com.andeva.atelier.platform.crm.interfaces.rest.resources.responses;

import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing a service appointment.
 *
 * @author Adiel Sanchez Santin
 */
public record AppointmentResource(
        UUID id,
        UUID tenantId,
        UUID branchId,
        UUID customerId,
        UUID vehicleId,
        Instant scheduledAt,
        int estimatedDurationMinutes,
        String reason,
        String status,
        String cancellationReason
) {}
