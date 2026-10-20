package com.andeva.atelier.platform.iot.application.internal.queryservices;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.VehicleHealthReportPdfGeneratorPort;
import com.andeva.atelier.platform.iot.application.queryservices.VehicleHealthReportQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.VehicleHealthReportNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleHealthReport;
import com.andeva.atelier.platform.iot.domain.model.queries.ExportFleetHealthReportPdfQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.ExportVehicleHealthReportPdfQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetLatestVehicleHealthReportQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetVehicleHealthReportByIdQuery;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleHealthReportRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public byte[] handle(ExportFleetHealthReportPdfQuery query) {
        Objects.requireNonNull(query, "ExportFleetHealthReportPdfQuery cannot be null");

        // The customer must belong to the requesting workshop before any of its vehicles is listed
        String customerName = crmFleetAclPort.getCustomerName(query.tenantId().value(), query.customerId())
                .orElseThrow(() -> new VehicleHealthReportNotFoundException("No existe el cliente " + query.customerId() + " en este taller"));
        List<CrmFleetAclPort.FleetVehicleDto> fleet = crmFleetAclPort.listCustomerFleet(query.customerId());
        Map<VehicleId, VehicleHealthReport> latest = reportRepository
                .findLatestByTenantIdAndVehicleIds(query.tenantId(), fleet.stream().map(CrmFleetAclPort.FleetVehicleDto::vehicleId).toList())
                .stream()
                .collect(Collectors.toMap(VehicleHealthReport::getVehicleId, report -> report));

        // Critical units first; vehicles without any report go last
        List<VehicleHealthReportPdfGeneratorPort.FleetEntry> entries = fleet.stream()
                .map(vehicle -> toEntry(vehicle, latest.get(vehicle.vehicleId())))
                .sorted(Comparator.comparing(VehicleHealthReportPdfGeneratorPort.FleetEntry::healthScore,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return pdfGeneratorPort.generateFleetReportPdf(customerName, entries);
    }

    private static VehicleHealthReportPdfGeneratorPort.FleetEntry toEntry(CrmFleetAclPort.FleetVehicleDto vehicle, VehicleHealthReport report) {
        String description = vehicle.brand() + " " + vehicle.model();
        if (report == null) {
            return new VehicleHealthReportPdfGeneratorPort.FleetEntry(vehicle.licensePlate(), description, null,
                    "SIN INFORME", "No se ha generado un informe pericial para esta unidad.", BigDecimal.ZERO, null);
        }
        BigDecimal budget = report.getDiagnosis().recommendedActions().stream()
                .map(action -> action.estimatedCost() != null ? action.estimatedCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new VehicleHealthReportPdfGeneratorPort.FleetEntry(vehicle.licensePlate(), description,
                report.getDiagnosis().healthScore(), report.getDiagnosis().overallCondition(),
                report.getDiagnosis().executiveSummary(), budget, report.getGeneratedAt());
    }
}
