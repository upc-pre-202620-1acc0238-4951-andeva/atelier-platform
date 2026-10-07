package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain query to retrieve historical telemetry records aggregated through TimescaleDB time-buckets.
 *
 * @author Joel Huamani Estefanero
 */
public record GetTelemetryHistoryQuery(
        VehicleId vehicleId,
        Instant from,
        Instant to,
        String bucketInterval
) implements Serializable {

    public GetTelemetryHistoryQuery {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(from, "from timestamp cannot be null");
        Objects.requireNonNull(to, "to timestamp cannot be null");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' timestamp cannot be before 'from' timestamp");
        }
        bucketInterval = bucketInterval != null ? bucketInterval : "1 hour";
    }
}
