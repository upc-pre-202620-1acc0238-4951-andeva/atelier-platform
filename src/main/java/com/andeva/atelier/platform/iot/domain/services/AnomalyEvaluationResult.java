package com.andeva.atelier.platform.iot.domain.services;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;

import java.io.Serializable;
import java.util.Objects;

/**
 * Diagnostic evaluation outcome produced by the PredictiveAnomalyDetectionEngine.
 *
 * @author Joel Huamani Estefanero
 */
public record AnomalyEvaluationResult(
        AlertType alertType,
        ConfidenceScore confidenceScore,
        String diagnosticMessage
) implements Serializable {

    public AnomalyEvaluationResult {
        Objects.requireNonNull(alertType, "AlertType cannot be null");
        Objects.requireNonNull(confidenceScore, "ConfidenceScore cannot be null");
        diagnosticMessage = diagnosticMessage != null ? diagnosticMessage : "";
    }
}
