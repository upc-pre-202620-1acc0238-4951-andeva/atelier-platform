package com.andeva.atelier.platform.iot.application.queryservices;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Query Service for reading real-time and historical time-bucketed telemetry records.
 *
 * @author Joel Huamani Estefanero
 */
public interface TelemetryLogQueryService {

    /**
     * Retrieves the most recent telemetry reading captured for the vehicle.
     *
     * @param vehicleId target vehicle identifier
     * @return optional TelemetryRecord
     */
    Optional<TelemetryRecord> getLatestTelemetry(VehicleId vehicleId);

    /**
     * Retrieves time-bucket aggregated telemetry data points over a given period.
     *
     * @param vehicleId      target vehicle identifier
     * @param from           window start timestamp
     * @param to             window end timestamp
     * @param bucketInterval interval string (e.g., "1 hour", "15 minutes")
     * @return list of aggregated telemetry records
     */
    List<TelemetryRecord> getAggregatedTelemetry(VehicleId vehicleId, Instant from, Instant to, String bucketInterval);
}
