package com.andeva.atelier.platform.crm.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request for cancelling an appointment with a justification.
 *
 * @author Adiel Sanchez Santin
 */
public record CancelAppointmentResource(
        @NotBlank(message = "Cancellation reason is mandatory")
        @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
        String cancellationReason
) {}
