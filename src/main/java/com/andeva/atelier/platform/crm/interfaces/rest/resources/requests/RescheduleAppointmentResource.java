package com.andeva.atelier.platform.crm.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * REST request for rescheduling an appointment.
 *
 * @author Adiel Sanchez Santin
 */
public record RescheduleAppointmentResource(
        @NotNull(message = "New scheduled timestamp is mandatory")
        @jakarta.validation.constraints.Future(message = "New scheduled timestamp must be in the future")
        Instant newScheduledAt
) {}
