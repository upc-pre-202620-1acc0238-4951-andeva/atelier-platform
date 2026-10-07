package com.andeva.atelier.platform.iot.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when an AI-assisted vehicle health diagnostic report is successfully generated.
 *
 * @param tenantId                workshop tenant identifier
 * @param vehicleId               target vehicle identifier
 * @param overallHealthScore      overall vehicle health score (0-100)
 * @param healthTrafficLight      traffic light status (GREEN, YELLOW, RED)
 * @param criticalAnomaliesCount  number of critical anomalies identified
 * @param occurredOn              timestamp of report generation
 * @author Joel Huamani Estefanero
 */
public record VehicleHealthReportGeneratedIntegrationEvent(
        UUID tenantId,
        UUID vehicleId,
        int overallHealthScore,
        String healthTrafficLight,
        int criticalAnomaliesCount,
        Instant occurredOn
) implements Serializable {
    public VehicleHealthReportGeneratedIntegrationEvent {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(healthTrafficLight, "healthTrafficLight cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
