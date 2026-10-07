package com.andeva.atelier.platform.iot.domain.model.dto.ai;

import java.io.Serializable;
import java.util.Objects;

/**
 * Diagnostic evaluation for a specific vehicle mechanical subsystem (Engine, Cooling, Electrical, Brakes, Transmission).
 *
 * @author Joel Huamani Estefanero
 */
public record SubsystemEvaluationDto(
        String subsystemName,
        String status,
        int score,
        String findings
) implements Serializable {

    public SubsystemEvaluationDto {
        subsystemName = subsystemName != null ? subsystemName : "General";
        status = status != null ? status : "UNKNOWN";
        findings = findings != null ? findings : "";
    }
}
