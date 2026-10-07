package com.andeva.atelier.platform.iot.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a bulk batch of vehicular telemetry readings is successfully persisted.
 *
 * @author Joel Huamani Estefanero
 */
public record TelemetryBatchIngestedEvent(
        VehicleId vehicleId,
        TenantId tenantId,
        int recordsCount,
        Instant latestTimestamp
) implements Serializable {

    public TelemetryBatchIngestedEvent {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(latestTimestamp, "latestTimestamp cannot be null");
        if (recordsCount < 0) {
            throw new IllegalArgumentException("Records count cannot be negative: " + recordsCount);
        }
    }

    public static TelemetryBatchIngestedEvent of(
            VehicleId vehicleId,
            TenantId tenantId,
            int recordsCount,
            Instant latestTimestamp
    ) {
        return new TelemetryBatchIngestedEvent(vehicleId, tenantId, recordsCount, latestTimestamp);
    }
}
