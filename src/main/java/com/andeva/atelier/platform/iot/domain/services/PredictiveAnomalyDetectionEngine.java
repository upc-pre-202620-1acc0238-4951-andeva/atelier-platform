package com.andeva.atelier.platform.iot.domain.services;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineTemperature;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
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
        Optional<Double> coolantTemp = record.engineTemperature().map(t -> t.celsius());
        if (record.engineRpm().isExcessiveRpm() && coolantTemp.filter(t -> t > 100.0).isPresent()) {
            return Optional.of(new AnomalyEvaluationResult(
                    AlertType.CYLINDER_MISFIRE_HAZARD,
                    new ConfidenceScore(new BigDecimal("85.00")),
                    String.format(
                            Locale.US,
                            "Revoluciones excesivas del motor (%d RPM) con temperatura elevada del refrigerante (%.1f°C). Alto riesgo de esfuerzo mecánico en los cilindros.",
                            record.engineRpm().rpm(),
                            coolantTemp.get()
                    )
            ));
        }

        return Optional.empty();
    }

    public Optional<AnomalyEvaluationResult> detectThermalRunaway(TelemetryRecord record) {
        Objects.requireNonNull(record, "TelemetryRecord cannot be null");
        Optional<EngineTemperature> coolant = record.engineTemperature();
        if (coolant.isPresent() && coolant.get().isCriticalOverheating()) {
            double temp = coolant.get().celsius();
            BigDecimal confidence = temp >= 115.0 ? new BigDecimal("98.50") : new BigDecimal("88.00");
            return Optional.of(new AnomalyEvaluationResult(
                    AlertType.ENGINE_OVERHEATING_RISK,
                    new ConfidenceScore(confidence),
                    String.format(
                            Locale.US,
                            "Temperatura crítica del refrigerante alcanzada (%.1f°C). Riesgo inminente de falla en la empaquetadura de culata.",
                            temp
                    )
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
                    String.format(
                            Locale.US,
                            "Voltaje peligroso de batería en reposo (%.2fV). Requiere recarga o reemplazo preventivo antes de una falla de arranque.",
                            voltage
                    )
            ));
        }
        return Optional.empty();
    }
}
