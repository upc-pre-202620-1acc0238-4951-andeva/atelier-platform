package com.andeva.atelier.platform.iot.domain.model.dto.ai;

import java.io.Serializable;

/**
 * Causal correlation between an active/historical DTC diagnostic code and sensory telemetry readings.
 *
 * @author Joel Huamani Estefanero
 */
public record DtcTelemetryCorrelationDto(
        String dtcCode,
        String correlatedSensorReading,
        String anomalyExplanation
) implements Serializable {

    public DtcTelemetryCorrelationDto {
        dtcCode = dtcCode != null ? dtcCode : "";
        correlatedSensorReading = correlatedSensorReading != null ? correlatedSensorReading : "";
        anomalyExplanation = anomalyExplanation != null ? anomalyExplanation : "";
    }
}
