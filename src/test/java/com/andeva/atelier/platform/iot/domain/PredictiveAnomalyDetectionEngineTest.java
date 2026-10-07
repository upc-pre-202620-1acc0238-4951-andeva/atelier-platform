package com.andeva.atelier.platform.iot.domain;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.*;
import com.andeva.atelier.platform.iot.domain.services.AnomalyEvaluationResult;
import com.andeva.atelier.platform.iot.domain.services.PredictiveAnomalyDetectionEngine;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for PredictiveAnomalyDetectionEngine.
 *
 * @author Joel Huamani Estefanero
 */
class PredictiveAnomalyDetectionEngineTest {

    private PredictiveAnomalyDetectionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PredictiveAnomalyDetectionEngine();
    }

    @Test
    @DisplayName("Should detect critical engine overheating with 88.00% confidence when temp is between 105 and 115 C")
    void testOverheatingModerate() {
        TelemetryRecord record = new TelemetryRecord(
                Instant.now(),
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                VehicleSpeed.of(50),
                EngineTemperature.of(108.0),
                EngineRpm.of(2500),
                Optional.empty(),
                Optional.of(BatteryVoltage.of(14.0))
        );

        Optional<AnomalyEvaluationResult> result = engine.evaluateTelemetryRecord(record);

        assertThat(result).isPresent();
        assertThat(result.get().alertType()).isEqualTo(AlertType.ENGINE_OVERHEATING_RISK);
        assertThat(result.get().confidenceScore().value()).isEqualTo(new BigDecimal("88.00"));
        assertThat(result.get().diagnosticMessage()).contains("108.0°C");
    }

    @Test
    @DisplayName("Should detect severe engine overheating with 98.50% confidence when temp is >= 115 C")
    void testOverheatingSevere() {
        TelemetryRecord record = new TelemetryRecord(
                Instant.now(),
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                VehicleSpeed.of(60),
                EngineTemperature.of(118.5),
                EngineRpm.of(3000),
                Optional.empty(),
                Optional.of(BatteryVoltage.of(13.8))
        );

        Optional<AnomalyEvaluationResult> result = engine.evaluateTelemetryRecord(record);

        assertThat(result).isPresent();
        assertThat(result.get().alertType()).isEqualTo(AlertType.ENGINE_OVERHEATING_RISK);
        assertThat(result.get().confidenceScore().value()).isEqualTo(new BigDecimal("98.50"));
    }

    @Test
    @DisplayName("Should detect dangerous low battery voltage when vehicle is stationary at rest")
    void testBatteryLowAtRest() {
        TelemetryRecord record = new TelemetryRecord(
                Instant.now(),
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                VehicleSpeed.of(0),
                EngineTemperature.of(88.0),
                EngineRpm.of(0),
                Optional.empty(),
                Optional.of(BatteryVoltage.of(11.4))
        );

        Optional<AnomalyEvaluationResult> result = engine.evaluateTelemetryRecord(record);

        assertThat(result).isPresent();
        assertThat(result.get().alertType()).isEqualTo(AlertType.BATTERY_FAILURE_RISK);
        assertThat(result.get().confidenceScore().value()).isEqualTo(new BigDecimal("91.20"));
        assertThat(result.get().diagnosticMessage()).contains("11.40V");
    }

    @Test
    @DisplayName("Should NOT trigger battery failure risk if vehicle is moving, even if voltage dips")
    void testBatteryLowWhileMoving() {
        TelemetryRecord record = new TelemetryRecord(
                Instant.now(),
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                VehicleSpeed.of(40), // moving
                EngineTemperature.of(90.0),
                EngineRpm.of(2000),
                Optional.empty(),
                Optional.of(BatteryVoltage.of(11.6))
        );

        Optional<AnomalyEvaluationResult> result = engine.evaluateTelemetryRecord(record);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should detect cylinder misfire / high mechanical strain on excessive RPM and high temp")
    void testExcessiveRpmStrain() {
        TelemetryRecord record = new TelemetryRecord(
                Instant.now(),
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                VehicleSpeed.of(120),
                EngineTemperature.of(102.0),
                EngineRpm.of(6500), // excessive RPM
                Optional.empty(),
                Optional.of(BatteryVoltage.of(14.0))
        );

        Optional<AnomalyEvaluationResult> result = engine.evaluateTelemetryRecord(record);

        assertThat(result).isPresent();
        assertThat(result.get().alertType()).isEqualTo(AlertType.CYLINDER_MISFIRE_HAZARD);
        assertThat(result.get().confidenceScore().value()).isEqualTo(new BigDecimal("85.00"));
    }

    @Test
    @DisplayName("Normal telemetry should return empty Optional")
    void testNormalTelemetry() {
        TelemetryRecord record = new TelemetryRecord(
                Instant.now(),
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                VehicleSpeed.of(80),
                EngineTemperature.of(90.0),
                EngineRpm.of(2200),
                Optional.of(FuelLevel.of(65.0)),
                Optional.of(BatteryVoltage.of(14.2))
        );

        Optional<AnomalyEvaluationResult> result = engine.evaluateTelemetryRecord(record);
        assertThat(result).isEmpty();
    }
}
