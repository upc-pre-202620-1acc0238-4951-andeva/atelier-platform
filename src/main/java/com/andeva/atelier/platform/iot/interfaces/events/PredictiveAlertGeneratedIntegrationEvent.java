package com.andeva.atelier.platform.iot.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a predictive mechanical or electrical alert is generated.
 * Consumed by CRM & Customer Experience and Workshop Operations (MRO).
 *
 * @param alertId                predictive alert identifier
 * @param tenantId               workshop tenant identifier
 * @param vehicleId              target vehicle identifier
 * @param alertType              category of predictive alert
 * @param severity               criticality level
 * @param confidenceScore        statistical or AI confidence percentage
 * @param suggestedServiceAction recommended maintenance action
 * @param occurredOn             timestamp of alert generation
 * @author Joel Huamani Estefanero
 */
public record PredictiveAlertGeneratedIntegrationEvent(
        UUID alertId,
        UUID tenantId,
        UUID vehicleId,
        String alertType,
        String severity,
        double confidenceScore,
        String suggestedServiceAction,
        Instant occurredOn
) implements Serializable {
    public PredictiveAlertGeneratedIntegrationEvent {
        Objects.requireNonNull(alertId, "alertId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(alertType, "alertType cannot be null");
        Objects.requireNonNull(severity, "severity cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
