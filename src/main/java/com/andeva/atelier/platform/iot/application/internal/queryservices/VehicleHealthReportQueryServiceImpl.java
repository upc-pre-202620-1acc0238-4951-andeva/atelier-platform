package com.andeva.atelier.platform.iot.application.internal.queryservices;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.AiInferenceDiagnosticPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.VehicleHealthReportPdfGeneratorPort;
import com.andeva.atelier.platform.iot.application.queryservices.VehicleHealthReportQueryService;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.domain.model.queries.ExportVehicleHealthReportPdfQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetLatestVehicleHealthReportQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Query Service implementation for vehicle health diagnostic report lookups and institutional PDF rendering.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class VehicleHealthReportQueryServiceImpl implements VehicleHealthReportQueryService {

    private final AiInferenceDiagnosticPort aiInferenceDiagnosticPort;
    private final VehicleHealthReportPdfGeneratorPort pdfGeneratorPort;
    private final CrmFleetAclPort crmFleetAclPort;

    public VehicleHealthReportQueryServiceImpl(
            AiInferenceDiagnosticPort aiInferenceDiagnosticPort,
            VehicleHealthReportPdfGeneratorPort pdfGeneratorPort,
            CrmFleetAclPort crmFleetAclPort
    ) {
        this.aiInferenceDiagnosticPort = Objects.requireNonNull(aiInferenceDiagnosticPort, "AiInferenceDiagnosticPort cannot be null");
        this.pdfGeneratorPort = Objects.requireNonNull(pdfGeneratorPort, "VehicleHealthReportPdfGeneratorPort cannot be null");
        this.crmFleetAclPort = Objects.requireNonNull(crmFleetAclPort, "CrmFleetAclPort cannot be null");
    }

    @Override
    public Optional<VehicleHealthReportAiDto> handle(GetLatestVehicleHealthReportQuery query) {
        Objects.requireNonNull(query, "GetLatestVehicleHealthReportQuery cannot be null");
        VehicleHealthReportAiDto report = aiInferenceDiagnosticPort.generateVehicleDiagnostic(
                query.tenantId(),
                query.vehicleId(),
                30,
                true
        );
        return Optional.ofNullable(report);
    }

    @Override
    public byte[] handle(ExportVehicleHealthReportPdfQuery query) {
        Objects.requireNonNull(query, "ExportVehicleHealthReportPdfQuery cannot be null");

        VehicleHealthReportAiDto report = aiInferenceDiagnosticPort.generateVehicleDiagnostic(
                query.tenantId(),
                query.vehicleId(),
                30,
                true
        );

        CrmFleetAclPort.VehicleMetadataDto metadata = crmFleetAclPort.getVehicleMetadata(query.vehicleId())
                .orElse(new CrmFleetAclPort.VehicleMetadataDto(
                        "UNKNOWN",
                        "UNKNOWN-VIN",
                        "Generic",
                        "Vehicle",
                        2024,
                        "Valued Customer"
                ));

        return pdfGeneratorPort.generateHealthReportPdf(report, metadata);
    }
}
