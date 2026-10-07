package com.andeva.atelier.platform.iot.domain;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.enums.RiskLevel;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.*;
import com.andeva.atelier.platform.iot.domain.services.ThermodynamicEvaluationResult;
import com.andeva.atelier.platform.iot.domain.services.VehicleThermodynamicEvaluationService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for VehicleThermodynamicEvaluationService.
 *
 * @author Joel Huamani Estefanero
 */
class VehicleThermodynamicEvaluationServiceTest {

    private VehicleThermodynamicEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new VehicleThermodynamicEvaluationService();
    }

    @Test
    @DisplayName("Should detect CRITICAL risk when coolant temperature is >= 105 C")
    void testCriticalOverheating() {
        ThermodynamicEvaluationResult result = service.evaluate(
                EngineRpm.of(3000),
                VehicleSpeed.of(60),
                EngineTemperature.of(107.0)
        );

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(result.isAbnormalThermalGradient()).isTrue();
        assertThat(result.diagnosticSummary()).contains("107.0°C");
    }

    @Test
    @DisplayName("Should detect HIGH risk when vehicle is stationary with elevated temp (> 98 C)")
    void testHighThermalLoadInTraffic() {
        ThermodynamicEvaluationResult result = service.evaluate(
                EngineRpm.of(800), // idling
                VehicleSpeed.of(0), // stationary
                EngineTemperature.of(99.5) // high temp
        );

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(result.isAbnormalThermalGradient()).isTrue();
        assertThat(result.diagnosticSummary()).contains("radiator fan or coolant circulation");
    }

    @Test
    @DisplayName("Should detect MODERATE risk when thermostat is stuck open (subcooled at high speed)")
    void testSubcoolingHighwayCruising() {
        ThermodynamicEvaluationResult result = service.evaluate(
                EngineRpm.of(2500),
                VehicleSpeed.of(90),
                EngineTemperature.of(62.0) // subcooled < 70 C
        );

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.MODERATE);
        assertThat(result.isAbnormalThermalGradient()).isTrue();
        assertThat(result.diagnosticSummary()).contains("Thermostat may be stuck open");
    }

    @Test
    @DisplayName("Should detect LOW risk when operating in normal thermodynamic equilibrium")
    void testNormalThermodynamics() {
        TelemetryRecord record = new TelemetryRecord(
                Instant.now(),
                VehicleId.generate(),
                TenantId.generate(),
                Optional.empty(),
                VehicleSpeed.of(70),
                EngineTemperature.of(89.0),
                EngineRpm.of(2100),
                Optional.empty(),
                Optional.empty()
        );

        ThermodynamicEvaluationResult result = service.evaluate(record);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(result.isAbnormalThermalGradient()).isFalse();
        assertThat(result.diagnosticSummary()).contains("normal thermodynamic parameters");
    }
}
