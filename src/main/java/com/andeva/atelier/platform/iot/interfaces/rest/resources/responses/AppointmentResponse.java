package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing a scheduled workshop appointment converted from a predictive alert.
 *
 * @author Joel Huamani Estefanero
 */
public record AppointmentResponse(
        UUID appointmentId,
        UUID vehicleId,
        UUID customerId,
        Instant scheduledAt,
        String status,
        String notes
) implements Serializable {
}
