package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleHealthReport;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.HealthReportCreatedResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.HealthReportDetailResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Transforms issued {@link VehicleHealthReport} aggregates into REST responses.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class VehicleHealthReportResourceAssembler {

    public HealthReportCreatedResponse toResponse(VehicleHealthReport report) {
        if (report == null) {
            return null;
        }

        UUID reportId = report.getId().value();
        VehicleHealthReportAiDto dto = report.getDiagnosis();
        int risksCount = dto.predictiveRisks() != null ? dto.predictiveRisks().size() : 0;

        return new HealthReportCreatedResponse(
                reportId,
                report.getVehicleId().value(),
                dto.healthScore(),
                dto.executiveSummary(),
                risksCount,
                report.getGeneratedAt(),
                jsonUrl(reportId),
                pdfUrl(reportId)
        );
    }

    public HealthReportDetailResponse toDetailResponse(VehicleHealthReport report) {
        if (report == null) {
            return null;
        }

        UUID reportId = report.getId().value();
        return new HealthReportDetailResponse(
                reportId,
                report.getVehicleId().value(),
                report.getDaysAnalyzed(),
                report.getGeneratedAt(),
                report.getDiagnosis(),
                pdfUrl(reportId)
        );
    }

    private static String jsonUrl(UUID reportId) {
        return "/api/v1/iot/health-reports/" + reportId;
    }

    private static String pdfUrl(UUID reportId) {
        return "/api/v1/iot/health-reports/" + reportId + "/pdf";
    }
}
