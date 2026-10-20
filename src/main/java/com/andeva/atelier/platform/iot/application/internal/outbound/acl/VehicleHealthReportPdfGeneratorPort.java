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

    /**
     * Renders the executive fleet consolidation: one row per vehicle with its latest health index,
     * critical units first, and the estimated preventive budget.
     *
     * @param customerName fleet owner
     * @param entries      one entry per vehicle of the fleet
     * @return raw binary PDF byte array
     */
    byte[] generateFleetReportPdf(String customerName, java.util.List<FleetEntry> entries);

    record FleetEntry(
            String licensePlate,
            String vehicle,
            Integer healthScore,
            String overallCondition,
            String executiveSummary,
            java.math.BigDecimal preventiveBudget,
            java.time.Instant generatedAt
    ) {
        public boolean isCritical() {
            return healthScore != null && healthScore < 60;
        }
    }
}
