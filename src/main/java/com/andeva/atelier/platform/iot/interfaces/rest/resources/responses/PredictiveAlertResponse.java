package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing a predictive maintenance alert dispatched to workshop advisors.
 *
 * @author Joel Huamani Estefanero
 */
public record PredictiveAlertResponse(
        UUID id,
        UUID vehicleId,
        UUID recommendedServiceId,
        String alertType,
        BigDecimal confidenceScore,
        String message,
        String status,
        Instant createdAt
) implements Serializable {
}
