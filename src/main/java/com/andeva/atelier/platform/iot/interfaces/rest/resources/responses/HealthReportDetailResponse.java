package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response exposing an issued vehicle health report with its full structured diagnosis.
 *
 * @param reportId       issued report identifier
 * @param vehicleId      evaluated vehicle identifier
 * @param daysAnalyzed   telemetry window, in days, that fed the diagnosis
 * @param generatedAt    issue timestamp
 * @param diagnosis      structured AI diagnosis (subsystems, risks, recommended actions, DTC correlations)
 * @param pdfDownloadUrl relative URL of the institutional PDF
 * @author Joel Huamani Estefanero
 */
public record HealthReportDetailResponse(
        UUID reportId,
        UUID vehicleId,
        int daysAnalyzed,
        Instant generatedAt,
        VehicleHealthReportAiDto diagnosis,
        String pdfDownloadUrl
) implements Serializable {
}
