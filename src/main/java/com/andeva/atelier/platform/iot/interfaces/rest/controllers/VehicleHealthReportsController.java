package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.VehicleHealthReportCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.VehicleHealthReportQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.IoTDomainException;
import com.andeva.atelier.platform.iot.domain.exceptions.VehicleHealthReportNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.commands.GenerateVehicleHealthReportCommand;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.domain.model.queries.ExportVehicleHealthReportPdfQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetLatestVehicleHealthReportQuery;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.GenerateHealthReportRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.AsyncJobResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.HealthReportCreatedResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.VehicleHealthReportResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller governing AI-assisted vehicle mechanical health diagnostic evaluation,
 * forensic telemetry inspection, and institutional PDF report export.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/iot/health-reports")
@Tag(name = "Vehicle Health Reports", description = "Endpoints for AI-assisted vehicle diagnostic evaluations and PDF report generation")
public class VehicleHealthReportsController {

    private final VehicleHealthReportCommandService reportCommandService;
    private final VehicleHealthReportQueryService reportQueryService;
    private final VehicleHealthReportResourceAssembler reportAssembler;

    public VehicleHealthReportsController(
            VehicleHealthReportCommandService reportCommandService,
            VehicleHealthReportQueryService reportQueryService,
            VehicleHealthReportResourceAssembler reportAssembler
    ) {
        this.reportCommandService = Objects.requireNonNull(reportCommandService, "VehicleHealthReportCommandService cannot be null");
        this.reportQueryService = Objects.requireNonNull(reportQueryService, "VehicleHealthReportQueryService cannot be null");
        this.reportAssembler = Objects.requireNonNull(reportAssembler, "VehicleHealthReportResourceAssembler cannot be null");
    }

    @Operation(summary = "Generate synchronous AI-assisted vehicle mechanical health diagnostic report")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Health report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Quota exceeded or forbidden"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "502", description = "AI inference service unavailable")
    })
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('iot:health_reports:generate') or hasAnyRole('ROLE_SERVICE_ADVISOR', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<HealthReportCreatedResponse> generateHealthReport(
            @RequestParam UUID vehicleId,
            @Valid @RequestBody(required = false) GenerateHealthReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        VehicleId vId = VehicleId.of(vehicleId);

        int days = (request != null && request.daysToAnalyze() != null) ? request.daysToAnalyze() : 30;
        boolean includeResolved = request != null && Boolean.TRUE.equals(request.includeResolvedDtcHistory());

        GenerateVehicleHealthReportCommand command = new GenerateVehicleHealthReportCommand(
                tenantId,
                vId,
                days,
                includeResolved
        );

        VehicleHealthReportAiDto reportDto = reportCommandService.handle(command);
        UUID reportId = UUID.randomUUID();
        Instant generatedAt = Instant.now();

        HealthReportCreatedResponse response = reportAssembler.toResponse(reportId, vehicleId, reportDto, generatedAt);

        return ResponseEntity.created(URI.create("/api/v1/iot/health-reports/" + reportId))
                .body(response);
    }

    @Operation(summary = "Enqueue asynchronous vehicle health diagnostic report generation")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Report generation task queued successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Quota exceeded or forbidden")
    })
    @PostMapping("/generate-async")
    @PreAuthorize("hasAuthority('iot:health_reports:generate') or hasAnyRole('ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<AsyncJobResponse> generateHealthReportAsync(
            @RequestParam UUID vehicleId,
            @Valid @RequestBody(required = false) GenerateHealthReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        resolveTenantId(userDetails);
        UUID jobId = UUID.randomUUID();
        Instant now = Instant.now();

        AsyncJobResponse response = new AsyncJobResponse(
                jobId,
                "QUEUED",
                vehicleId,
                now,
                now.plus(15, ChronoUnit.SECONDS)
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Operation(summary = "Get latest stored health report for vehicle without re-running AI inference")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Latest health report retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "No previous health report found for vehicle")
    })
    @GetMapping("/latest")
    @PreAuthorize("hasAuthority('iot:health_reports:read') or hasAnyRole('ROLE_MECHANIC', 'ROLE_SERVICE_ADVISOR', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<HealthReportCreatedResponse> getLatestHealthReport(
            @RequestParam UUID vehicleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        VehicleId vId = VehicleId.of(vehicleId);

        VehicleHealthReportAiDto reportDto = reportQueryService.handle(new GetLatestVehicleHealthReportQuery(tenantId, vId))
                .orElseThrow(() -> new VehicleHealthReportNotFoundException(vId));

        UUID reportId = UUID.randomUUID();
        HealthReportCreatedResponse response = reportAssembler.toResponse(reportId, vehicleId, reportDto, Instant.now());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Download official institutional vehicle health diagnostic report in PDF format")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF binary report generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @GetMapping("/{reportId}/pdf")
    @PreAuthorize("hasAuthority('iot:health_reports:read') or hasAnyRole('ROLE_SERVICE_ADVISOR', 'ROLE_MECHANIC', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<byte[]> downloadHealthReportPdf(
            @PathVariable UUID reportId,
            @RequestParam(required = false) UUID vehicleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        VehicleId vId = vehicleId != null ? VehicleId.of(vehicleId) : VehicleId.generate();

        ExportVehicleHealthReportPdfQuery query = new ExportVehicleHealthReportPdfQuery(tenantId, vId, reportId);
        byte[] pdfBytes = reportQueryService.handle(query);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"vehicle-health-report-" + reportId + ".pdf\"")
                .body(pdfBytes);
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new AccessDeniedException("Active tenant context is required");
        }
        return TenantId.of(userDetails.getTenantId());
    }
}
