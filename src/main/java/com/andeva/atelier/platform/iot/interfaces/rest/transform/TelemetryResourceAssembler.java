package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.BatteryVoltage;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.FuelLevel;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.TelemetryHistoryBucketResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.VehicleLatestTelemetryResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Transforms {@link TelemetryRecord} domain entities into latest telemetry and history bucket REST responses.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class TelemetryResourceAssembler {

    public VehicleLatestTelemetryResponse toLatestResponse(TelemetryRecord record) {
        if (record == null) {
            return null;
        }

        return new VehicleLatestTelemetryResponse(
                record.vehicleId().value(),
                record.timestamp(),
                record.location().map(GeoCoordinates::latitude).orElse(null),
                record.location().map(GeoCoordinates::longitude).orElse(null),
                record.speed().kmh(),
                record.engineTemperature().celsius(),
                record.engineRpm().rpm(),
                record.batteryVoltage().map(BatteryVoltage::volts).orElse(null),
                record.fuelLevel().map(FuelLevel::percentage).orElse(null)
        );
    }

    public TelemetryHistoryBucketResponse toBucketResponse(
            Instant bucketTime,
            UUID vehicleId,
            int avgSpeedKmh,
            double avgEngineTempCelsius,
            int avgEngineRpm,
            Double avgFuelPercentage,
            Double avgBatteryVoltage,
            int sampleCount
    ) {
        return new TelemetryHistoryBucketResponse(
                bucketTime,
                vehicleId,
                avgSpeedKmh,
                avgEngineTempCelsius,
                avgEngineRpm,
                avgFuelPercentage,
                avgBatteryVoltage,
                sampleCount
        );
    }
}
