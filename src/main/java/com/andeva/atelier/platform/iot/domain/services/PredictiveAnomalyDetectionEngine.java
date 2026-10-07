package com.andeva.atelier.platform.iot.domain.services;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * Pure domain service engine evaluating incoming vehicle telemetry readings in real-time
 * to identify incipient thermal and electrical anomalies deterministically before catastrophic failure.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class PredictiveAnomalyDetectionEngine {

    /**
     * Evaluates an instantaneous telemetry record against critical physical safety thresholds.
     *
     * @param record incoming immutable telemetry reading
     * @return Optional containing AnomalyEvaluationResult if an anomaly is diagnosed, empty otherwise
     */
    public Optional<AnomalyEvaluationResult> evaluateTelemetryRecord(TelemetryRecord record) {
        Objects.requireNonNull(record, "TelemetryRecord cannot be null");

        // 1. Critical Engine Overheating Detection
        Optional<AnomalyEvaluationResult> thermalResult = detectThermalRunaway(record);
        if (thermalResult.isPresent()) {
            return thermalResult;
        }

        // 2. Battery & Alternator Degradation Detection at Rest
        Optional<AnomalyEvaluationResult> electricalResult = detectAlternatorFailure(record);
        if (electricalResult.isPresent()) {
            return electricalResult;
        }

        // 3. High Mechanical Strain (Excessive RPM under High Temperature)
        if (record.engineRpm().isExcessiveRpm() && record.engineTemperature().celsius() > 100.0) {
            return Optional.of(new AnomalyEvaluationResult(
                    AlertType.CYLINDER_MISFIRE_HAZARD,
                    new ConfidenceScore(new BigDecimal("85.00")),
                    String.format(
                            "Excessive engine RPM (%d RPM) under elevated coolant temperature (%.1f°C). High risk of mechanical cylinder strain.",
                            record.engineRpm().rpm(),
                            record.engineTemperature().celsius()
                    )
            ));
        }

        return Optional.empty();
    }

    public Optional<AnomalyEvaluationResult> detectThermalRunaway(TelemetryRecord record) {
        Objects.requireNonNull(record, "TelemetryRecord cannot be null");
        if (record.engineTemperature().isCriticalOverheating()) {
            double temp = record.engineTemperature().celsius();
            BigDecimal confidence = temp >= 115.0 ? new BigDecimal("98.50") : new BigDecimal("88.00");
            return Optional.of(new AnomalyEvaluationResult(
                    AlertType.ENGINE_OVERHEATING_RISK,
                    new ConfidenceScore(confidence),
                    String.format("Critical coolant temperature reached (%.1f°C). Imminent risk of cylinder head gasket failure.", temp)
            ));
        }
        return Optional.empty();
    }

    public Optional<AnomalyEvaluationResult> detectAlternatorFailure(TelemetryRecord record) {
        Objects.requireNonNull(record, "TelemetryRecord cannot be null");
        if (record.batteryVoltage().isPresent() && record.batteryVoltage().get().isLowBattery() && record.speed().kmh() == 0) {
            double voltage = record.batteryVoltage().get().volts();
            return Optional.of(new AnomalyEvaluationResult(
                    AlertType.BATTERY_FAILURE_RISK,
                    new ConfidenceScore(new BigDecimal("91.20")),
                    String.format("Dangerous battery voltage at rest (%.2fV). Requires recharge or preventive battery replacement before starting failure.", voltage)
            ));
        }
        return Optional.empty();
    }
}
