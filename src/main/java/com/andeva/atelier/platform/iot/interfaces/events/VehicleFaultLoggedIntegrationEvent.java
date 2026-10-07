package com.andeva.atelier.platform.iot.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a new DTC vehicle fault is logged by the ECU.
 *
 * @param faultId     unique fault identifier
 * @param tenantId    workshop tenant identifier
 * @param vehicleId   target vehicle identifier
 * @param dtcCode     standard SAE DTC code
 * @param severity    fault severity level
 * @param description fault description
 * @param occurredOn  timestamp of fault detection
 * @author Joel Huamani Estefanero
 */
public record VehicleFaultLoggedIntegrationEvent(
        UUID faultId,
        UUID tenantId,
        UUID vehicleId,
        String dtcCode,
        String severity,
        String description,
        Instant occurredOn
) implements Serializable {
    public VehicleFaultLoggedIntegrationEvent {
        Objects.requireNonNull(faultId, "faultId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(dtcCode, "dtcCode cannot be null");
        Objects.requireNonNull(severity, "severity cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
