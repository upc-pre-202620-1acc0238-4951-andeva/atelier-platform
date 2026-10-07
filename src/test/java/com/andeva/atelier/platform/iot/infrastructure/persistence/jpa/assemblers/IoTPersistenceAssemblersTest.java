package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iot.domain.model.aggregates.*;
import com.andeva.atelier.platform.iot.domain.model.entities.DtcCatalogEntry;
import com.andeva.atelier.platform.iot.domain.model.enums.*;
import com.andeva.atelier.platform.iot.domain.model.ids.*;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.*;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.*;
import com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.assemblers.TimescaleTelemetryPersistenceAssembler;
import com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.entities.TelemetryLogPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for all IoT persistence assemblers.
 *
 * @author Joel Huamani Estefanero
 */
class IoTPersistenceAssemblersTest {

    private final TenantId tenantId = TenantId.generate();
    private final VehicleId vehicleId = VehicleId.generate();
    private final DeviceId deviceId = DeviceId.generate();

    @Test
    @DisplayName("Obd2DevicePersistenceAssembler should correctly map domain to entity and back")
    void testObd2DevicePersistenceAssembler() {
        Obd2DevicePersistenceAssembler assembler = new Obd2DevicePersistenceAssembler();

        Obd2Device device = new Obd2Device(
                deviceId,
                tenantId,
                DeviceIdentifier.of("00:1B:44:11:3A:B7"),
                ConnectionType.BLUETOOTH_BLE,
                DeviceStatus.ACTIVE,
                "ELM327-v2.2",
                "2.2.0"
        );

        Obd2DevicePersistenceEntity entity = assembler.toEntity(device);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(deviceId.value());
        assertThat(entity.getTenantId()).isEqualTo(tenantId.value());
        assertThat(entity.getDeviceIdentifier()).isEqualTo("00:1B:44:11:3A:B7");
        assertThat(entity.getConnectionType()).isEqualTo(ConnectionType.BLUETOOTH_BLE);
        assertThat(entity.getStatus()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(entity.getHardwareModel()).isEqualTo("ELM327-v2.2");
        assertThat(entity.getFirmwareVersion()).isEqualTo("2.2.0");

        Obd2Device mappedBack = assembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.getId()).isEqualTo(deviceId);
        assertThat(mappedBack.getTenantId()).isEqualTo(tenantId);
        assertThat(mappedBack.getDeviceIdentifier().value()).isEqualTo("00:1B:44:11:3A:B7");
        assertThat(mappedBack.getConnectionType()).isEqualTo(ConnectionType.BLUETOOTH_BLE);
        assertThat(mappedBack.getStatus()).isEqualTo(DeviceStatus.ACTIVE);

        assertThat(assembler.toEntity(null)).isNull();
        assertThat(assembler.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("DeviceInstallationPersistenceAssembler should correctly map domain to entity and back")
    void testDeviceInstallationPersistenceAssembler() {
        DeviceInstallationPersistenceAssembler assembler = new DeviceInstallationPersistenceAssembler();
        InstallationId installationId = InstallationId.generate();
        Instant now = Instant.now();

        DeviceInstallation installation = new DeviceInstallation(
                installationId,
                deviceId,
                vehicleId,
                tenantId,
                now,
                Optional.of(now.plusSeconds(3600)),
                12000,
                Optional.of(12350)
        );

        DeviceInstallationPersistenceEntity entity = assembler.toEntity(installation);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(installationId.value());
        assertThat(entity.getDeviceId()).isEqualTo(deviceId.value());
        assertThat(entity.getVehicleId()).isEqualTo(vehicleId.value());
        assertThat(entity.getTenantId()).isEqualTo(tenantId.value());
        assertThat(entity.getInstalledAt()).isEqualTo(now);
        assertThat(entity.getUninstalledAt()).isEqualTo(now.plusSeconds(3600));
        assertThat(entity.getInitialOdometerKm()).isEqualTo(12000);
        assertThat(entity.getFinalOdometerKm()).isEqualTo(12350);

        DeviceInstallation mappedBack = assembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.getId()).isEqualTo(installationId);
        assertThat(mappedBack.getDeviceId()).isEqualTo(deviceId);
        assertThat(mappedBack.getVehicleId()).isEqualTo(vehicleId);
        assertThat(mappedBack.getTenantId()).isEqualTo(tenantId);
        assertThat(mappedBack.getFinalOdometerKm()).contains(12350);

        assertThat(assembler.toEntity(null)).isNull();
        assertThat(assembler.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("VehicleFaultPersistenceAssembler should correctly map domain to entity and back")
    void testVehicleFaultPersistenceAssembler() {
        VehicleFaultPersistenceAssembler assembler = new VehicleFaultPersistenceAssembler();
        FaultId faultId = FaultId.generate();
        Instant now = Instant.now();

        VehicleFault fault = new VehicleFault(
                faultId,
                vehicleId,
                tenantId,
                DtcCode.of("P0300"),
                FaultSeverity.CRITICAL,
                "Random/Multiple Cylinder Misfire Detected",
                now,
                true,
                Optional.of(now.plusSeconds(1800))
        );

        VehicleFaultPersistenceEntity entity = assembler.toEntity(fault);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(faultId.value());
        assertThat(entity.getVehicleId()).isEqualTo(vehicleId.value());
        assertThat(entity.getDtcCode()).isEqualTo("P0300");
        assertThat(entity.getSeverity()).isEqualTo(FaultSeverity.CRITICAL);
        assertThat(entity.isResolved()).isTrue();
        assertThat(entity.getResolvedAt()).isEqualTo(now.plusSeconds(1800));

        VehicleFault mappedBack = assembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.getId()).isEqualTo(faultId);
        assertThat(mappedBack.getDtcCode().value()).isEqualTo("P0300");
        assertThat(mappedBack.isResolved()).isTrue();
        assertThat(mappedBack.getResolvedAt()).contains(now.plusSeconds(1800));

        assertThat(assembler.toEntity(null)).isNull();
        assertThat(assembler.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("PredictiveAlertPersistenceAssembler should correctly map domain to entity and back")
    void testPredictiveAlertPersistenceAssembler() {
        PredictiveAlertPersistenceAssembler assembler = new PredictiveAlertPersistenceAssembler();
        AlertId alertId = AlertId.generate();
        ServiceId serviceId = ServiceId.generate();
        Instant now = Instant.now();

        PredictiveAlert alert = new PredictiveAlert(
                alertId,
                vehicleId,
                tenantId,
                Optional.of(serviceId),
                AlertType.ENGINE_OVERHEATING_RISK,
                ConfidenceScore.of(new BigDecimal("94.50")),
                "Critical temperature reached",
                AlertStatus.DISPATCHED,
                Optional.of("fcm-msg-123"),
                now
        );

        PredictiveAlertPersistenceEntity entity = assembler.toEntity(alert);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(alertId.value());
        assertThat(entity.getVehicleId()).isEqualTo(vehicleId.value());
        assertThat(entity.getRecommendedServiceId()).isEqualTo(serviceId.value());
        assertThat(entity.getAlertType()).isEqualTo(AlertType.ENGINE_OVERHEATING_RISK);
        assertThat(entity.getConfidenceScore()).isEqualByComparingTo(new BigDecimal("94.50"));
        assertThat(entity.getFcmMessageId()).isEqualTo("fcm-msg-123");

        PredictiveAlert mappedBack = assembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.getId()).isEqualTo(alertId);
        assertThat(mappedBack.getRecommendedServiceId()).contains(serviceId);
        assertThat(mappedBack.getConfidenceScore().percentage()).isEqualTo(94.50);
        assertThat(mappedBack.getFcmMessageId()).contains("fcm-msg-123");

        assertThat(assembler.toEntity(null)).isNull();
        assertThat(assembler.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("DtcCatalogPersistenceAssembler should correctly map domain to entity and back")
    void testDtcCatalogPersistenceAssembler() {
        DtcCatalogPersistenceAssembler assembler = new DtcCatalogPersistenceAssembler();
        DtcCatalogId catalogId = DtcCatalogId.generate();

        DtcCatalogEntry entry = new DtcCatalogEntry(
                catalogId,
                DtcCode.of("P0420"),
                DtcCategory.POWERTRAIN_P,
                "Catalyst System Efficiency Below Threshold",
                FaultSeverity.MEDIUM
        );

        DtcCatalogEntryPersistenceEntity entity = assembler.toEntity(entry);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(catalogId.value());
        assertThat(entity.getDtcCode()).isEqualTo("P0420");
        assertThat(entity.getSystemCategory()).isEqualTo(DtcCategory.POWERTRAIN_P);
        assertThat(entity.getDefaultSeverity()).isEqualTo(FaultSeverity.MEDIUM);

        DtcCatalogEntry mappedBack = assembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.getId()).isEqualTo(catalogId);
        assertThat(mappedBack.getCode().value()).isEqualTo("P0420");
        assertThat(mappedBack.getCategory()).isEqualTo(DtcCategory.POWERTRAIN_P);

        assertThat(assembler.toEntity(null)).isNull();
        assertThat(assembler.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("TimescaleTelemetryPersistenceAssembler should correctly map domain to entity and back")
    void testTimescaleTelemetryPersistenceAssembler() {
        TimescaleTelemetryPersistenceAssembler assembler = new TimescaleTelemetryPersistenceAssembler();
        Instant now = Instant.now();

        TelemetryRecord record = TelemetryRecord.of(
                now,
                vehicleId,
                tenantId,
                GeoCoordinates.of(-12.046374, -77.042793),
                VehicleSpeed.of(85.0),
                EngineTemperature.of(98.5),
                EngineRpm.of(2800),
                FuelLevel.of(75.0),
                BatteryVoltage.of(14.2)
        );

        TelemetryLogPersistenceEntity entity = assembler.toEntity(record);
        assertThat(entity).isNotNull();
        assertThat(entity.getId().getTimestamp()).isEqualTo(now);
        assertThat(entity.getId().getVehicleId()).isEqualTo(vehicleId.value());
        assertThat(entity.getTenantId()).isEqualTo(tenantId.value());
        assertThat(entity.getLatitude()).isEqualTo(-12.046374);
        assertThat(entity.getLongitude()).isEqualTo(-77.042793);
        assertThat(entity.getSpeed()).isEqualTo(85);
        assertThat(entity.getEngineTempC()).isEqualTo(98.5);
        assertThat(entity.getEngineRpm()).isEqualTo(2800);
        assertThat(entity.getFuelLevelPct()).isEqualTo(75.0);
        assertThat(entity.getBatteryVoltage()).isEqualTo(14.2);

        TelemetryRecord mappedBack = assembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.timestamp()).isEqualTo(now);
        assertThat(mappedBack.vehicleId()).isEqualTo(vehicleId);
        assertThat(mappedBack.tenantId()).isEqualTo(tenantId);
        assertThat(mappedBack.location()).isPresent();
        assertThat(mappedBack.speed().kmh()).isEqualTo(85);
        assertThat(mappedBack.engineTemperature().celsius()).isEqualTo(98.5);
        assertThat(mappedBack.batteryVoltage().get().volts()).isEqualTo(14.2);

        assertThat(assembler.toEntity(null)).isNull();
        assertThat(assembler.toDomain(null)).isNull();
    }
}
