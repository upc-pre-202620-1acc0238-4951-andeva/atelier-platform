package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing an AI-assisted vehicle mechanical health diagnostic report.
 *
 * @author Joel Huamani Estefanero
 */
public record HealthReportCreatedResponse(
        UUID reportId,
        UUID vehicleId,
        int overallHealthScore,
        String executiveSummary,
        int totalRisksDetected,
        Instant generatedAt,
        String jsonResourceUrl,
        String pdfDownloadUrl
) implements Serializable {
}
