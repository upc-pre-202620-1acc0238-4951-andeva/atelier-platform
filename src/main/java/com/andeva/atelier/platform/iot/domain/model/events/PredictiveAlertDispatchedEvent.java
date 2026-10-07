package com.andeva.atelier.platform.iot.domain.model.events;

import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a predictive maintenance alert is dispatched via Firebase Cloud Messaging push notification.
 *
 * @author Joel Huamani Estefanero
 */
public record PredictiveAlertDispatchedEvent(
        AlertId alertId,
        VehicleId vehicleId,
        TenantId tenantId,
        String fcmMessageId,
        Instant dispatchedAt
) implements Serializable {

    public PredictiveAlertDispatchedEvent {
        Objects.requireNonNull(alertId, "AlertId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(dispatchedAt, "dispatchedAt timestamp cannot be null");
    }

    public static PredictiveAlertDispatchedEvent of(
            AlertId alertId,
            VehicleId vehicleId,
            TenantId tenantId,
            String fcmMessageId
    ) {
        return new PredictiveAlertDispatchedEvent(alertId, vehicleId, tenantId, fcmMessageId, Instant.now());
    }
}
