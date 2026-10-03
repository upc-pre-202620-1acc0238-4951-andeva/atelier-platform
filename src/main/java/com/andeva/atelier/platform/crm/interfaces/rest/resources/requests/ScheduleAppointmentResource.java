package com.andeva.atelier.platform.crm.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * REST request for scheduling a service appointment.
 *
 * @author Adiel Sanchez Santin
 */
public record ScheduleAppointmentResource(
        @NotNull(message = "Branch ID is mandatory")
        UUID branchId,

        @NotNull(message = "Customer ID is mandatory")
        UUID customerId,

        @NotNull(message = "Vehicle ID is mandatory")
        UUID vehicleId,

        @NotNull(message = "Scheduled timestamp is mandatory")
        Instant scheduledAt,

        int estimatedDurationMinutes,

        @Size(max = 1000, message = "Reason must not exceed 1000 characters")
        String reason
) {}
