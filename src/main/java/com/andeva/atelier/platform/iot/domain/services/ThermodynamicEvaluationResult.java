package com.andeva.atelier.platform.iot.domain.services;

import com.andeva.atelier.platform.iot.domain.model.enums.RiskLevel;

import java.io.Serializable;
import java.util.Objects;

/**
 * Diagnostic result of thermodynamic and kinematic correlation analysis on vehicular powertrain telemetry.
 *
 * @author Joel Huamani Estefanero
 */
public record ThermodynamicEvaluationResult(
        RiskLevel riskLevel,
        boolean isAbnormalThermalGradient,
        String diagnosticSummary
) implements Serializable {

    public ThermodynamicEvaluationResult {
        Objects.requireNonNull(riskLevel, "RiskLevel cannot be null");
        diagnosticSummary = diagnosticSummary != null ? diagnosticSummary : "";
    }
}
