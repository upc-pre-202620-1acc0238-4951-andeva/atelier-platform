package com.andeva.atelier.platform.iot.interfaces.acl.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Immutable DTO representing an aggregated health score and traffic light evaluation for a vehicle.
 *
 * @author Joel Huamani Estefanero
 */
public record VehicleTelemetryHealthDto(
        UUID vehicleId,
        int score,
        String trafficLight,
        int faultCount,
        boolean requiresAttention
) implements Serializable {
}
