package com.andeva.atelier.platform.iot.domain.model.events;

import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a driver or workshop service advisor acknowledges receipt of a predictive alert.
 *
 * @author Joel Huamani Estefanero
 */
public record PredictiveAlertAcknowledgedEvent(
        AlertId alertId,
        VehicleId vehicleId,
        Instant acknowledgedAt
) implements Serializable {

    public PredictiveAlertAcknowledgedEvent {
        Objects.requireNonNull(alertId, "AlertId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(acknowledgedAt, "acknowledgedAt timestamp cannot be null");
    }

    public static PredictiveAlertAcknowledgedEvent of(AlertId alertId, VehicleId vehicleId) {
        return new PredictiveAlertAcknowledgedEvent(alertId, vehicleId, Instant.now());
    }
}
