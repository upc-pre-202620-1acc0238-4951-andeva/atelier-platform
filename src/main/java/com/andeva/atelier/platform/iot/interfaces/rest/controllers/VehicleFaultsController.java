package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.VehicleFaultCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.VehicleFaultQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.VehicleFaultNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.commands.RegisterVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.ResolveVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.RegisterVehicleFaultRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.VehicleFaultResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.VehicleFaultResourceAssembler;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller governing the lifecycle of vehicle diagnostic trouble codes (DTC),
 * computerized engine scans, active fault logs, and workshop resolution confirmations.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/iot/faults")
@Tag(name = "Vehicle Faults", description = "Endpoints for managing ECU diagnostic trouble codes (DTC) and fault lifecycle")
public class VehicleFaultsController {

    private final VehicleFaultCommandService faultCommandService;
    private final VehicleFaultQueryService faultQueryService;
    private final VehicleFaultResourceAssembler faultAssembler;

    public VehicleFaultsController(
            VehicleFaultCommandService faultCommandService,
            VehicleFaultQueryService faultQueryService,
            VehicleFaultResourceAssembler faultAssembler
    ) {
        this.faultCommandService = Objects.requireNonNull(faultCommandService, "VehicleFaultCommandService cannot be null");
        this.faultQueryService = Objects.requireNonNull(faultQueryService, "VehicleFaultQueryService cannot be null");
        this.faultAssembler = Objects.requireNonNull(faultAssembler, "VehicleFaultResourceAssembler cannot be null");
    }

    @Operation(summary = "Register new electronic diagnostic trouble code (DTC) for vehicle")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehicle fault registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid DTC code format or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('iot:faults:write') or hasAnyRole('ROLE_MECHANIC', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<VehicleFaultResponse> registerVehicleFault(
            @Valid @RequestBody RegisterVehicleFaultRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        VehicleId vehicleId = VehicleId.of(request.vehicleId());

        DtcCode code = DtcCode.of(request.dtcCode());
        FaultSeverity severity = FaultSeverity.valueOf(request.severity().toUpperCase().trim());

        RegisterVehicleFaultCommand command = new RegisterVehicleFaultCommand(
                vehicleId,
                tenantId,
                code,
                severity,
                request.description()
        );

        FaultId faultId = faultCommandService.handle(command);
        VehicleFault fault = faultQueryService.findById(faultId)
                .orElseThrow(() -> new VehicleFaultNotFoundException(faultId));

        return ResponseEntity.created(URI.create("/api/v1/iot/faults/" + faultId.value()))
                .body(faultAssembler.toResponse(fault));
    }

    @Operation(summary = "Get all active unresolved faults for a vehicle")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active faults retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/vehicle/{vehicleId}/active")
    @PreAuthorize("hasAuthority('iot:faults:read') or hasAnyRole('ROLE_SERVICE_ADVISOR', 'ROLE_MECHANIC', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<List<VehicleFaultResponse>> getActiveFaultsByVehicle(
            @PathVariable UUID vehicleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        VehicleId vId = VehicleId.of(vehicleId);

        List<VehicleFault> faults = faultQueryService.getActiveFaultsByVehicle(vId);
        if (faults.stream().anyMatch(f -> !f.getTenantId().equals(tenantId))) {
            throw new AccessDeniedException("Vehicle faults do not belong to the authenticated workshop");
        }
        return ResponseEntity.ok(faults.stream().map(faultAssembler::toResponse).toList());
    }

    @Operation(summary = "Resolve and close an electronic vehicle fault")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle fault resolved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Vehicle fault not found")
    })
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('iot:faults:resolve') or hasAnyRole('ROLE_MECHANIC', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<VehicleFaultResponse> resolveVehicleFault(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        FaultId faultId = FaultId.of(id);

        VehicleFault fault = faultQueryService.findById(faultId)
                .orElseThrow(() -> new VehicleFaultNotFoundException(faultId));

        if (!fault.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Fault record does not belong to the authenticated workshop");
        }

        faultCommandService.handle(new ResolveVehicleFaultCommand(faultId));

        VehicleFault resolved = faultQueryService.findById(faultId)
                .orElseThrow(() -> new VehicleFaultNotFoundException(faultId));

        return ResponseEntity.ok(faultAssembler.toResponse(resolved));
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new AccessDeniedException("Active tenant context is required");
        }
        return TenantId.of(userDetails.getTenantId());
    }
}
