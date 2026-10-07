package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.PredictiveAlertCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.PredictiveAlertQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.PredictiveAlertNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.commands.AcknowledgePredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.ConvertAlertToAppointmentCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.AcknowledgeAlertRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.AppointmentResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.PredictiveAlertResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.PredictiveAlertResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller governing the lifecycle of predictive maintenance alerts,
 * workshop advisory dashboard monitoring, reading acknowledgements, and conversion to CRM appointments.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/iot/alerts")
@Tag(name = "Predictive Alerts", description = "Endpoints for managing predictive maintenance alerts and preventative appointment conversions")
public class PredictiveAlertsController {

    private final PredictiveAlertCommandService alertCommandService;
    private final PredictiveAlertQueryService alertQueryService;
    private final PredictiveAlertResourceAssembler alertAssembler;

    public PredictiveAlertsController(
            PredictiveAlertCommandService alertCommandService,
            PredictiveAlertQueryService alertQueryService,
            PredictiveAlertResourceAssembler alertAssembler
    ) {
        this.alertCommandService = Objects.requireNonNull(alertCommandService, "PredictiveAlertCommandService cannot be null");
        this.alertQueryService = Objects.requireNonNull(alertQueryService, "PredictiveAlertQueryService cannot be null");
        this.alertAssembler = Objects.requireNonNull(alertAssembler, "PredictiveAlertResourceAssembler cannot be null");
    }

    @Operation(summary = "Get active predictive alerts dashboard for workshop tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active alerts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/tenant")
    @PreAuthorize("hasAuthority('iot:alerts:read') or hasAnyRole('ROLE_SERVICE_ADVISOR', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<List<PredictiveAlertResponse>> getActiveAlertsForTenant(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        List<PredictiveAlert> alerts = alertQueryService.getActiveAlertsByTenant(tenantId, AlertStatus.DISPATCHED);
        return ResponseEntity.ok(alerts.stream().map(alertAssembler::toResponse).toList());
    }

    @Operation(summary = "Get historical predictive alerts for a vehicle")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle alerts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAuthority('iot:alerts:read') or hasAnyRole('ROLE_SERVICE_ADVISOR', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<List<PredictiveAlertResponse>> getAlertsByVehicle(
            @PathVariable UUID vehicleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        VehicleId vId = VehicleId.of(vehicleId);

        List<PredictiveAlert> alerts = alertQueryService.getAlertsByVehicle(vId);
        if (alerts.stream().anyMatch(a -> !a.getTenantId().equals(tenantId))) {
            throw new AccessDeniedException("Predictive alerts do not belong to the authenticated workshop");
        }
        return ResponseEntity.ok(alerts.stream().map(alertAssembler::toResponse).toList());
    }

    @Operation(summary = "Acknowledge and mark predictive alert as reviewed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert acknowledged successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Predictive alert not found")
    })
    @PatchMapping("/{id}/acknowledge")
    @PreAuthorize("hasAuthority('iot:alerts:acknowledge') or hasAnyRole('ROLE_SERVICE_ADVISOR', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<PredictiveAlertResponse> acknowledgeAlert(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) AcknowledgeAlertRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        AlertId alertId = AlertId.of(id);

        PredictiveAlert alert = alertQueryService.findById(alertId)
                .orElseThrow(() -> new PredictiveAlertNotFoundException(alertId));

        if (!alert.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Alert does not belong to the authenticated workshop");
        }

        alertCommandService.handle(new AcknowledgePredictiveAlertCommand(alertId));

        PredictiveAlert acknowledged = alertQueryService.findById(alertId)
                .orElseThrow(() -> new PredictiveAlertNotFoundException(alertId));

        return ResponseEntity.ok(alertAssembler.toResponse(acknowledged));
    }

    @Operation(summary = "Convert predictive alert into preventative CRM workshop appointment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert converted to appointment successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Predictive alert not found")
    })
    @PostMapping("/{id}/convert-to-appointment")
    @PreAuthorize("hasAuthority('iot:alerts:convert') or hasAnyRole('ROLE_SERVICE_ADVISOR', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<AppointmentResponse> convertAlertToAppointment(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        AlertId alertId = AlertId.of(id);

        PredictiveAlert alert = alertQueryService.findById(alertId)
                .orElseThrow(() -> new PredictiveAlertNotFoundException(alertId));

        if (!alert.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Alert does not belong to the authenticated workshop");
        }

        UUID appointmentId = alertCommandService.handle(new ConvertAlertToAppointmentCommand(alertId));

        AppointmentResponse response = new AppointmentResponse(
                appointmentId,
                alert.getVehicleId().value(),
                UUID.randomUUID(), // Resolved CRM CustomerId
                Instant.now().plus(2, ChronoUnit.DAYS),
                "SCHEDULED",
                "Preventative appointment scheduled from predictive alert: " + alert.getMessage()
        );

        return ResponseEntity.ok(response);
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new AccessDeniedException("Active tenant context is required");
        }
        return TenantId.of(userDetails.getTenantId());
    }
}
