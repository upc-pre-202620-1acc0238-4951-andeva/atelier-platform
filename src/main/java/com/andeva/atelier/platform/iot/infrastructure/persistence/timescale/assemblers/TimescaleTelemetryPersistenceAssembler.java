package com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.assemblers;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.BatteryVoltage;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineRpm;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineTemperature;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.FuelLevel;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.VehicleSpeed;
import com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.entities.TelemetryLogPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Assembler for translating between domain {@link TelemetryRecord} time-series aggregates
 * and TimescaleDB {@link TelemetryLogPersistenceEntity} hypertable records.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class TimescaleTelemetryPersistenceAssembler {

    public TelemetryRecord toDomain(TelemetryLogPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        Optional<GeoCoordinates> location = (entity.getLatitude() != null && entity.getLongitude() != null)
                ? Optional.of(new GeoCoordinates(entity.getLatitude(), entity.getLongitude()))
                : Optional.empty();

        return new TelemetryRecord(
                entity.getId().getTimestamp(),
                new VehicleId(entity.getId().getVehicleId()),
                new TenantId(entity.getTenantId()),
                location,
                new VehicleSpeed(entity.getSpeed()),
                new EngineTemperature(entity.getEngineTempC()),
                new EngineRpm(entity.getEngineRpm()),
                Optional.ofNullable(entity.getFuelLevelPct()).map(FuelLevel::new),
                Optional.ofNullable(entity.getBatteryVoltage()).map(BatteryVoltage::new)
        );
    }

    public TelemetryLogPersistenceEntity toEntity(TelemetryRecord domain) {
        if (domain == null) {
            return null;
        }
        var entity = new TelemetryLogPersistenceEntity();
        entity.setId(new TelemetryLogPersistenceEntity.TelemetryRecordId(
                domain.timestamp(),
                domain.vehicleId().value()
        ));
        entity.setTenantId(domain.tenantId().value());
        entity.setLatitude(domain.location().map(GeoCoordinates::latitude).orElse(null));
        entity.setLongitude(domain.location().map(GeoCoordinates::longitude).orElse(null));
        entity.setSpeed(domain.speed().kmh());
        entity.setEngineTempC(domain.engineTemperature().celsius());
        entity.setEngineRpm(domain.engineRpm().rpm());
        entity.setFuelLevelPct(domain.fuelLevel().map(FuelLevel::percentage).orElse(null));
        entity.setBatteryVoltage(domain.batteryVoltage().map(BatteryVoltage::volts).orElse(null));
        return entity;
    }
}
