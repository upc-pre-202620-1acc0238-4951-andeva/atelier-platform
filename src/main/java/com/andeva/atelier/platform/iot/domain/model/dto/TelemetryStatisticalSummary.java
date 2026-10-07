package com.andeva.atelier.platform.iot.domain.model.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Aggregated statistical summary of telemetry readings extracted from TimescaleDB hypertable
 * time-bucket intervals, used for algorithmic anomaly detection and AI report generation.
 *
 * @author Joel Huamani Estefanero
 */
public record TelemetryStatisticalSummary(
        int recordsCount,
        double avgSpeed,
        double maxSpeed,
        double avgEngineTemp,
        double maxEngineTemp,
        int avgRpm,
        int maxRpm,
        double minBatteryVoltage,
        Instant bucketStart,
        Instant bucketEnd
) implements Serializable {

    public TelemetryStatisticalSummary {
        Objects.requireNonNull(bucketStart, "bucketStart cannot be null");
        Objects.requireNonNull(bucketEnd, "bucketEnd cannot be null");
    }
}
