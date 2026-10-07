package com.andeva.atelier.platform.iot.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.Serializable;

/**
 * Request DTO specifying parameters for AI-assisted vehicle mechanical health diagnostic report generation.
 *
 * @author Joel Huamani Estefanero
 */
public record GenerateHealthReportRequest(
        @Min(value = 7, message = "{error.iot.health_report.days.min}")
        @Max(value = 90, message = "{error.iot.health_report.days.max}")
        Integer daysToAnalyze,

        Boolean includeResolvedDtcHistory,

        String triggerReason
) implements Serializable {

    public GenerateHealthReportRequest {
        if (daysToAnalyze == null) {
            daysToAnalyze = 30;
        }
        if (includeResolvedDtcHistory == null) {
            includeResolvedDtcHistory = false;
        }
    }

    public GenerateHealthReportRequest(Integer daysToAnalyze, Boolean includeResolvedDtcHistory) {
        this(daysToAnalyze, includeResolvedDtcHistory, null);
    }
}
