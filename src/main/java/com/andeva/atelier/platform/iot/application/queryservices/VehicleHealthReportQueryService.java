package com.andeva.atelier.platform.iot.application.queryservices;

import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.domain.model.queries.ExportVehicleHealthReportPdfQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetLatestVehicleHealthReportQuery;

import java.util.Optional;

/**
 * Query Service for consulting latest vehicle diagnostic reports and generating PDF exports.
 *
 * @author Joel Huamani Estefanero
 */
public interface VehicleHealthReportQueryService {

    /**
     * Retrieves the latest stored AI-assisted vehicle health diagnostic report.
     *
     * @param query query with target vehicle identifier
     * @return optional VehicleHealthReportAiDto
     */
    Optional<VehicleHealthReportAiDto> handle(GetLatestVehicleHealthReportQuery query);

    /**
     * Generates and exports a complete institutional PDF document for a vehicle diagnostic report.
     *
     * @param query query with target vehicle identifier and report options
     * @return raw binary PDF byte array
     */
    byte[] handle(ExportVehicleHealthReportPdfQuery query);
}
