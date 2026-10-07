package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.HealthReportCreatedResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Transforms {@link VehicleHealthReportAiDto} domain representations into {@link HealthReportCreatedResponse} REST responses.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class VehicleHealthReportResourceAssembler {

    public HealthReportCreatedResponse toResponse(
            UUID reportId,
            UUID vehicleId,
            VehicleHealthReportAiDto dto,
            Instant generatedAt
    ) {
        if (dto == null) {
            return null;
        }

        int risksCount = dto.predictiveRisks() != null ? dto.predictiveRisks().size() : 0;
        String jsonUrl = "/api/v1/iot/health-reports/" + reportId;
        String pdfUrl = "/api/v1/iot/health-reports/" + reportId + "/pdf";

        return new HealthReportCreatedResponse(
                reportId,
                vehicleId,
                dto.healthScore(),
                dto.executiveSummary(),
                risksCount,
                generatedAt != null ? generatedAt : Instant.now(),
                jsonUrl,
                pdfUrl
        );
    }
}
