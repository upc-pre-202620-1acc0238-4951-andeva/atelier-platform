package com.andeva.atelier.platform.iot.application.internal.outbound.acl;

import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;

/**
 * Outbound port for rendering institutional Vehicle Health Reports in PDF format via OpenPDF.
 *
 * @author Joel Huamani Estefanero
 */
public interface VehicleHealthReportPdfGeneratorPort {

    /**
     * Renders a complete institutional PDF document for a vehicle health report.
     *
     * @param reportData structured AI diagnostic evaluation
     * @param metadata   vehicle technical and ownership metadata
     * @return raw binary PDF byte array
     */
    byte[] generateHealthReportPdf(
            VehicleHealthReportAiDto reportData,
            CrmFleetAclPort.VehicleMetadataDto metadata
    );
}
