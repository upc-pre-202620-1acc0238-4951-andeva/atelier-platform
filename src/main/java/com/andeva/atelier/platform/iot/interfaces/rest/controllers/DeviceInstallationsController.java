package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.DeviceInstallationCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.DeviceInstallationQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.InstallationNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.commands.InstallDeviceOnVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.UninstallDeviceFromVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.iot.domain.model.queries.GetDeviceByVehicleIdQuery;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.InstallDeviceRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.UninstallDeviceRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.DeviceInstallationResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.DeviceInstallationResourceAssembler;
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
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller governing the physical and operational coupling of OBD-II scanners
 * to customer vehicles in the automotive workshop.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/iot/installations")
@Tag(name = "Device Installations", description = "Endpoints for coupling and uncoupling OBD-II scanners on vehicles")
public class DeviceInstallationsController {

    private final DeviceInstallationCommandService installationCommandService;
    private final DeviceInstallationQueryService installationQueryService;
    private final DeviceInstallationResourceAssembler installationAssembler;

    public DeviceInstallationsController(
            DeviceInstallationCommandService installationCommandService,
            DeviceInstallationQueryService installationQueryService,
            DeviceInstallationResourceAssembler installationAssembler
    ) {
        this.installationCommandService = Objects.requireNonNull(installationCommandService, "DeviceInstallationCommandService cannot be null");
        this.installationQueryService = Objects.requireNonNull(installationQueryService, "DeviceInstallationQueryService cannot be null");
        this.installationAssembler = Objects.requireNonNull(installationAssembler, "DeviceInstallationResourceAssembler cannot be null");
    }

    @Operation(summary = "Install OBD-II device on vehicle")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device installed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Quota exceeded or forbidden"),
            @ApiResponse(responseCode = "404", description = "Device or vehicle not found"),
            @ApiResponse(responseCode = "409", description = "Active installation conflict")
    })
    @PostMapping("/install")
    @PreAuthorize("hasAuthority('iot:installations:manage') or hasAnyRole('ROLE_MECHANIC', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<DeviceInstallationResponse> installDevice(
            @Valid @RequestBody InstallDeviceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);

        InstallDeviceOnVehicleCommand command = new InstallDeviceOnVehicleCommand(
                DeviceId.of(request.deviceId()),
                VehicleId.of(request.vehicleId()),
                tenantId,
                request.currentOdometerKm()
        );

        InstallationId installationId = installationCommandService.handle(command);
        DeviceInstallation installation = installationQueryService.findById(installationId)
                .orElseThrow(() -> new InstallationNotFoundException(installationId));

        return ResponseEntity.created(URI.create("/api/v1/iot/installations/" + installationId.value()))
                .body(installationAssembler.toResponse(installation));
    }

    @Operation(summary = "Uninstall OBD-II device from vehicle")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device uninstalled successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or final odometer invalid"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Installation session not found")
    })
    @PostMapping("/{id}/uninstall")
    @PreAuthorize("hasAuthority('iot:installations:manage') or hasAnyRole('ROLE_MECHANIC', 'ROLE_HEAD_MECHANIC', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<DeviceInstallationResponse> uninstallDevice(
            @PathVariable UUID id,
            @Valid @RequestBody UninstallDeviceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        InstallationId installationId = InstallationId.of(id);

        DeviceInstallation installation = installationQueryService.findById(installationId)
                .orElseThrow(() -> new InstallationNotFoundException(installationId));

        if (!installation.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Installation does not belong to the authenticated workshop");
        }

        UninstallDeviceFromVehicleCommand command = new UninstallDeviceFromVehicleCommand(
                installationId,
                request.finalOdometerKm()
        );

        installationCommandService.handle(command);

        DeviceInstallation uninstalled = installationQueryService.findById(installationId)
                .orElseThrow(() -> new InstallationNotFoundException(installationId));

        return ResponseEntity.ok(installationAssembler.toResponse(uninstalled));
    }

    @Operation(summary = "Get active device installation for vehicle")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active installation retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "No active installation found")
    })
    @GetMapping("/vehicle/{vehicleId}/active")
    @PreAuthorize("hasAuthority('iot:installations:read') or hasAnyRole('ROLE_MECHANIC', 'ROLE_SERVICE_ADVISOR', 'ROLE_HEAD_MECHANIC')")
    public ResponseEntity<DeviceInstallationResponse> getActiveInstallationByVehicle(
            @PathVariable UUID vehicleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        VehicleId vId = VehicleId.of(vehicleId);

        DeviceInstallation installation = installationQueryService.handle(new GetDeviceByVehicleIdQuery(vId))
                .orElseThrow(() -> new InstallationNotFoundException("No active installation found for vehicle: " + vehicleId));

        if (!installation.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Vehicle installation does not belong to the authenticated workshop");
        }

        return ResponseEntity.ok(installationAssembler.toResponse(installation));
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new AccessDeniedException("Active tenant context is required");
        }
        return TenantId.of(userDetails.getTenantId());
    }
}
