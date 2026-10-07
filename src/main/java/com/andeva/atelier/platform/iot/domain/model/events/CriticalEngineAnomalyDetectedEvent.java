package com.andeva.atelier.platform.iot.domain.model.events;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when sensory telemetry signals breach critical thermodynamic or electrical thresholds.
 *
 * @author Joel Huamani Estefanero
 */
public record CriticalEngineAnomalyDetectedEvent(
        VehicleId vehicleId,
        TenantId tenantId,
        AlertType type,
        ConfidenceScore score,
        String message,
        Instant occurredOn
) implements Serializable {

    public CriticalEngineAnomalyDetectedEvent {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(type, "AlertType cannot be null");
        Objects.requireNonNull(score, "ConfidenceScore cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
        message = message != null ? message : "";
    }

    public static CriticalEngineAnomalyDetectedEvent of(
            VehicleId vehicleId,
            TenantId tenantId,
            AlertType type,
            ConfidenceScore score,
            String message
    ) {
        return new CriticalEngineAnomalyDetectedEvent(vehicleId, tenantId, type, score, message, Instant.now());
    }
}
