package com.andeva.atelier.platform.iot.domain.model.aggregates;

import com.andeva.atelier.platform.iot.domain.model.valueobjects.*;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable time-series sensory telemetry reading captured by an OBD-II scanner
 * and optimized for append-only ingestion into TimescaleDB hypertables.
 *
 * @author Joel Huamani Estefanero
 */
public record TelemetryRecord(
        Instant timestamp,
        VehicleId vehicleId,
        TenantId tenantId,
        Optional<GeoCoordinates> location,
        VehicleSpeed speed,
        EngineTemperature engineTemperature,
        EngineRpm engineRpm,
        Optional<FuelLevel> fuelLevel,
        Optional<BatteryVoltage> batteryVoltage
) implements Serializable {

    public TelemetryRecord {
        Objects.requireNonNull(timestamp, "Telemetry timestamp cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        location = location != null ? location : Optional.empty();
        Objects.requireNonNull(speed, "VehicleSpeed cannot be null");
        Objects.requireNonNull(engineTemperature, "EngineTemperature cannot be null");
        Objects.requireNonNull(engineRpm, "EngineRpm cannot be null");
        fuelLevel = fuelLevel != null ? fuelLevel : Optional.empty();
        batteryVoltage = batteryVoltage != null ? batteryVoltage : Optional.empty();
    }

    public static TelemetryRecord of(
            Instant timestamp,
            VehicleId vehicleId,
            TenantId tenantId,
            GeoCoordinates location,
            VehicleSpeed speed,
            EngineTemperature engineTemperature,
            EngineRpm engineRpm,
            FuelLevel fuelLevel,
            BatteryVoltage batteryVoltage
    ) {
        return new TelemetryRecord(
                timestamp,
                vehicleId,
                tenantId,
                Optional.ofNullable(location),
                speed,
                engineTemperature,
                engineRpm,
                Optional.ofNullable(fuelLevel),
                Optional.ofNullable(batteryVoltage)
        );
    }

    public boolean indicatesOverheating() {
        return engineTemperature.indicatesOverheating();
    }

    public boolean indicatesLowBattery() {
        return batteryVoltage.map(BatteryVoltage::isLowBattery).orElse(false);
    }

    public boolean isExcessiveRpm() {
        return engineRpm.isExcessiveRpm();
    }

    public boolean isEngineRunning() {
        return !engineRpm.isEngineStopped();
    }
}
