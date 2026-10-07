package com.andeva.atelier.platform.iot.domain.repositories;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.dto.TelemetryStatisticalSummary;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Outbound domain repository port for high-frequency telemetry time-series persistence in TimescaleDB.
 *
 * @author Joel Huamani Estefanero
 */
public interface TelemetryLogRepository {

    void saveAllBatch(List<TelemetryRecord> records);

    Optional<TelemetryRecord> findLatestByVehicleId(VehicleId vehicleId);

    List<TelemetryRecord> findHistoryAggregated(VehicleId vehicleId, Instant from, Instant to, String timeBucket);

    Optional<TelemetryStatisticalSummary> calculateStatisticalSummary(VehicleId vehicleId, Instant from, Instant to);
}
