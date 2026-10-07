package com.andeva.atelier.platform.iot.domain.services;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.enums.RiskLevel;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineRpm;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineTemperature;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.VehicleSpeed;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Domain service evaluating internal combustion thermodynamic gradients and kinematic heat dissipation.
 * Evaluates dynamic correlations between rotational engine speed (EngineRpm), linear vehicle velocity
 * (VehicleSpeed), and coolant thermal inertia (EngineTemperature) to diagnose impending thermostat,
 * radiator fan, or water pump failures before catastrophic engine seizure.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class VehicleThermodynamicEvaluationService {

    /**
     * Evaluates instantaneous thermodynamic telemetry indicators.
     *
     * @param rpm rotational engine speed
     * @param speed vehicle linear velocity
     * @param coolantTemp engine coolant temperature
     * @return ThermodynamicEvaluationResult containing evaluated risk and diagnostic summary
     */
    public ThermodynamicEvaluationResult evaluate(
            EngineRpm rpm,
            VehicleSpeed speed,
            EngineTemperature coolantTemp
    ) {
        Objects.requireNonNull(rpm, "EngineRpm cannot be null");
        Objects.requireNonNull(speed, "VehicleSpeed cannot be null");
        Objects.requireNonNull(coolantTemp, "EngineTemperature cannot be null");

        // 1. Critical Overheating: temperature >= 105°C
        if (coolantTemp.isCriticalOverheating()) {
            return new ThermodynamicEvaluationResult(
                    RiskLevel.CRITICAL,
                    true,
                    String.format(
                            "Critical coolant temperature (%.1f°C) detected at %d RPM and %d km/h. High risk of cylinder head warping.",
                            coolantTemp.celsius(),
                            rpm.rpm(),
                            speed.kmh()
                    )
            );
        }

        // 2. High Thermal Load in Traffic / Rest (Radiator fan or water pump anomaly)
        // High temp (> 98°C) while vehicle is stationary (speed == 0) and idling
        if (coolantTemp.celsius() > 98.0 && speed.isStationary() && rpm.isIdling()) {
            return new ThermodynamicEvaluationResult(
                    RiskLevel.HIGH,
                    true,
                    String.format(
                            "Elevated coolant temperature (%.1f°C) while vehicle is stationary at %d RPM. Potential radiator fan or coolant circulation insufficiency.",
                            coolantTemp.celsius(),
                            rpm.rpm()
                    )
            );
        }

        // 3. Thermostat Stuck Open (Subcooled engine during highway cruising)
        // Cruising at high speed (>= 80 km/h) for combustion engine with temp < 70°C
        if (speed.kmh() >= 80 && rpm.rpm() >= 2000 && coolantTemp.celsius() < 70.0) {
            return new ThermodynamicEvaluationResult(
                    RiskLevel.MODERATE,
                    true,
                    String.format(
                            "Subcooling detected (%.1f°C) during cruising at %d km/h and %d RPM. Thermostat may be stuck open, increasing emissions and fuel consumption.",
                            coolantTemp.celsius(),
                            speed.kmh(),
                            rpm.rpm()
                    )
            );
        }

        // 4. Normal Thermodynamic Operation
        return new ThermodynamicEvaluationResult(
                RiskLevel.LOW,
                false,
                String.format(
                        "Thermal dissipation operating within normal thermodynamic parameters (%.1f°C at %d RPM, %d km/h).",
                        coolantTemp.celsius(),
                        rpm.rpm(),
                        speed.kmh()
                )
        );
    }

    /**
     * Overloaded convenience method evaluating a TelemetryRecord aggregate.
     */
    public ThermodynamicEvaluationResult evaluate(TelemetryRecord record) {
        Objects.requireNonNull(record, "TelemetryRecord cannot be null");
        return evaluate(record.engineRpm(), record.speed(), record.engineTemperature());
    }
}
