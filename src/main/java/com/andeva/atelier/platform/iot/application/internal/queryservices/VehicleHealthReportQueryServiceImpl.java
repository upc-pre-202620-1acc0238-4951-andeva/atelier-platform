package com.andeva.atelier.platform.iot.application.internal.queryservices;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.VehicleHealthReportPdfGeneratorPort;
import com.andeva.atelier.platform.iot.application.queryservices.VehicleHealthReportQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.VehicleHealthReportNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleHealthReport;
import com.andeva.atelier.platform.iot.domain.model.queries.ExportVehicleHealthReportPdfQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetLatestVehicleHealthReportQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetVehicleHealthReportByIdQuery;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleHealthReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Query Service implementation for issued vehicle health reports and institutional PDF rendering.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class VehicleHealthReportQueryServiceImpl implements VehicleHealthReportQueryService {

    private final VehicleHealthReportRepository reportRepository;
    private final VehicleHealthReportPdfGeneratorPort pdfGeneratorPort;
    private final CrmFleetAclPort crmFleetAclPort;

    public VehicleHealthReportQueryServiceImpl(
            VehicleHealthReportRepository reportRepository,
            VehicleHealthReportPdfGeneratorPort pdfGeneratorPort,
            CrmFleetAclPort crmFleetAclPort
    ) {
        this.reportRepository = Objects.requireNonNull(reportRepository, "VehicleHealthReportRepository cannot be null");
        this.pdfGeneratorPort = Objects.requireNonNull(pdfGeneratorPort, "VehicleHealthReportPdfGeneratorPort cannot be null");
        this.crmFleetAclPort = Objects.requireNonNull(crmFleetAclPort, "CrmFleetAclPort cannot be null");
    }

    @Override
    public Optional<VehicleHealthReport> handle(GetLatestVehicleHealthReportQuery query) {
        Objects.requireNonNull(query, "GetLatestVehicleHealthReportQuery cannot be null");
        return reportRepository.findLatestByTenantIdAndVehicleId(query.tenantId(), query.vehicleId());
    }

    @Override
    public Optional<VehicleHealthReport> handle(GetVehicleHealthReportByIdQuery query) {
        Objects.requireNonNull(query, "GetVehicleHealthReportByIdQuery cannot be null");
        // A report of another workshop is reported as missing rather than leaking its existence
        return reportRepository.findById(query.reportId()).filter(report -> report.belongsTo(query.tenantId()));
    }

    @Override
    public byte[] handle(ExportVehicleHealthReportPdfQuery query) {
        Objects.requireNonNull(query, "ExportVehicleHealthReportPdfQuery cannot be null");

        VehicleHealthReport report = handle(new GetVehicleHealthReportByIdQuery(query.tenantId(), query.reportId()))
                .orElseThrow(() -> new VehicleHealthReportNotFoundException(
                        "No existe un reporte pericial de salud vehicular con ID " + query.reportId().value()));

        CrmFleetAclPort.VehicleMetadataDto metadata = crmFleetAclPort.getVehicleMetadata(report.getVehicleId())
                .orElse(new CrmFleetAclPort.VehicleMetadataDto(
                        "UNKNOWN",
                        "UNKNOWN-VIN",
                        "Generic",
                        "Vehicle",
                        2024,
                        "Valued Customer"
                ));

        return pdfGeneratorPort.generateHealthReportPdf(report.getDiagnosis(), metadata);
    }
}
