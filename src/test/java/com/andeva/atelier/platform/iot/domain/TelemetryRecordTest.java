package com.andeva.atelier.platform.iot.domain;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.*;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for the TelemetryRecord immutable aggregate.
 *
 * @author Joel Huamani Estefanero
 */
class TelemetryRecordTest {

    @Test
    @DisplayName("TelemetryRecord factory method should populate all fields and optional values")
    void testTelemetryRecordCreation() {
        Instant timestamp = Instant.now();
        VehicleId vehicleId = VehicleId.generate();
        TenantId tenantId = TenantId.generate();
        GeoCoordinates coords = GeoCoordinates.of(-12.046374, -77.042793);
        VehicleSpeed speed = VehicleSpeed.of(60);
        EngineTemperature temp = EngineTemperature.of(92.0);
        EngineRpm rpm = EngineRpm.of(2200);
        FuelLevel fuel = FuelLevel.of(75.0);
        BatteryVoltage battery = BatteryVoltage.of(14.2);

        TelemetryRecord record = TelemetryRecord.of(
                timestamp,
                vehicleId,
                tenantId,
                coords,
                speed,
                temp,
                rpm,
                fuel,
                battery
        );

        assertThat(record.timestamp()).isEqualTo(timestamp);
        assertThat(record.vehicleId()).isEqualTo(vehicleId);
        assertThat(record.tenantId()).isEqualTo(tenantId);
        assertThat(record.location()).contains(coords);
        assertThat(record.speed()).isEqualTo(speed);
        assertThat(record.engineTemperature()).isEqualTo(temp);
        assertThat(record.engineRpm()).isEqualTo(rpm);
        assertThat(record.fuelLevel()).contains(fuel);
        assertThat(record.batteryVoltage()).contains(battery);

        assertThat(record.indicatesOverheating()).isFalse();
        assertThat(record.indicatesLowBattery()).isFalse();
        assertThat(record.isExcessiveRpm()).isFalse();
        assertThat(record.isEngineRunning()).isTrue();
    }

    @Test
    @DisplayName("Domain evaluations on anomalous telemetry readings")
    void testAnomalousReadings() {
        TelemetryRecord overheatingRecord = new TelemetryRecord(
                Instant.now(),
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                VehicleSpeed.of(0),
                EngineTemperature.of(108.0),
                EngineRpm.of(800),
                Optional.empty(),
                Optional.of(BatteryVoltage.of(11.2))
        );

        assertThat(overheatingRecord.indicatesOverheating()).isTrue();
        assertThat(overheatingRecord.indicatesLowBattery()).isTrue();
        assertThat(overheatingRecord.isEngineRunning()).isTrue();
    }

    @Test
    @DisplayName("Null mandatory fields should throw NullPointerException")
    void testNullMandatoryFields() {
        assertThatThrownBy(() -> new TelemetryRecord(
                null,
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                VehicleSpeed.of(0),
                EngineTemperature.of(90.0),
                EngineRpm.of(1000),
                Optional.empty(),
                Optional.empty()
        )).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("timestamp");
    }
}
