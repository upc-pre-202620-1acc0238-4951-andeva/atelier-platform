package com.andeva.atelier.platform.iot.application.internal.queryservices;

import com.andeva.atelier.platform.iot.application.queryservices.TelemetryLogQueryService;
import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.repositories.TelemetryLogRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Query Service implementation for retrieving real-time and aggregated historical telemetry.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class TelemetryLogQueryServiceImpl implements TelemetryLogQueryService {

    private final TelemetryLogRepository telemetryLogRepository;

    public TelemetryLogQueryServiceImpl(TelemetryLogRepository telemetryLogRepository) {
        this.telemetryLogRepository = Objects.requireNonNull(telemetryLogRepository, "TelemetryLogRepository cannot be null");
    }

    @Override
    public Optional<TelemetryRecord> getLatestTelemetry(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        return telemetryLogRepository.findLatestByVehicleId(vehicleId);
    }

    @Override
    public List<TelemetryRecord> getAggregatedTelemetry(
            VehicleId vehicleId,
            Instant from,
            Instant to,
            String bucketInterval
    ) {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(from, "from timestamp cannot be null");
        Objects.requireNonNull(to, "to timestamp cannot be null");
        String interval = (bucketInterval != null && !bucketInterval.isBlank()) ? bucketInterval : "1 hour";
        return telemetryLogRepository.findHistoryAggregated(vehicleId, from, to, interval);
    }
}
