package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing aggregated historical telemetry metrics calculated via TimescaleDB time_bucket.
 *
 * @author Joel Huamani Estefanero
 */
public record TelemetryHistoryBucketResponse(
        Instant bucketTime,
        UUID vehicleId,
        int avgSpeedKmh,
        double avgEngineTempCelsius,
        int avgEngineRpm,
        Double avgFuelPercentage,
        Double avgBatteryVoltage,
        int sampleCount
) implements Serializable {
}
