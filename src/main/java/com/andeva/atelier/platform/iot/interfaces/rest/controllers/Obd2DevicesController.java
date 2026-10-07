package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.Obd2DeviceCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.Obd2DeviceQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.DeviceNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.commands.RegisterObd2DeviceCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.UpdateDeviceStatusCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.queries.GetDeviceByIdQuery;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.RegisterDeviceRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.UpdateDeviceStatusRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.responses.Obd2DeviceResponse;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.Obd2DeviceResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller governing automotive workshop OBD-II scanner inventory, registration,
 * status transitions, and hardware telemetry telemetry provisioning.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/iot/devices")
@Tag(name = "OBD-II Devices", description = "Endpoints for managing workshop OBD-II scanner hardware inventory")
public class Obd2DevicesController {

    private final Obd2DeviceCommandService deviceCommandService;
    private final Obd2DeviceQueryService deviceQueryService;
    private final Obd2DeviceResourceAssembler deviceAssembler;

    public Obd2DevicesController(
            Obd2DeviceCommandService deviceCommandService,
            Obd2DeviceQueryService deviceQueryService,
            Obd2DeviceResourceAssembler deviceAssembler
    ) {
        this.deviceCommandService = Objects.requireNonNull(deviceCommandService, "Obd2DeviceCommandService cannot be null");
        this.deviceQueryService = Objects.requireNonNull(deviceQueryService, "Obd2DeviceQueryService cannot be null");
        this.deviceAssembler = Objects.requireNonNull(deviceAssembler, "Obd2DeviceResourceAssembler cannot be null");
    }

    @Operation(summary = "Register new OBD-II device in workshop inventory")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Device identifier already exists or quota exceeded")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('iot:devices:register') or hasAnyRole('ROLE_WORKSHOP_OWNER', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<Obd2DeviceResponse> registerDevice(
            @Valid @RequestBody RegisterDeviceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);

        ConnectionType connectionType = ConnectionType.valueOf(request.connectionType().toUpperCase().trim());
        DeviceIdentifier identifier = DeviceIdentifier.of(request.deviceIdentifier());

        RegisterObd2DeviceCommand command = new RegisterObd2DeviceCommand(
                tenantId,
                identifier,
                connectionType,
                request.hardwareModel(),
                request.firmwareVersion()
        );

        DeviceId deviceId = deviceCommandService.handle(command);
        Obd2Device device = deviceQueryService.handle(new GetDeviceByIdQuery(deviceId))
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        return ResponseEntity.created(URI.create("/api/v1/iot/devices/" + deviceId.value()))
                .body(deviceAssembler.toResponse(device));
    }

    @Operation(summary = "Get OBD-II device by identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('iot:devices:read') or hasAnyRole('ROLE_HEAD_MECHANIC', 'ROLE_TENANT_ADMIN', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<Obd2DeviceResponse> getDeviceById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        DeviceId deviceId = DeviceId.of(id);

        Obd2Device device = deviceQueryService.handle(new GetDeviceByIdQuery(deviceId))
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        if (!device.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Device does not belong to the authenticated workshop");
        }

        return ResponseEntity.ok(deviceAssembler.toResponse(device));
    }

    @Operation(summary = "List workshop OBD-II devices with optional status filter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices listed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('iot:devices:read') or hasAnyRole('ROLE_HEAD_MECHANIC', 'ROLE_TENANT_ADMIN', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<List<Obd2DeviceResponse>> listDevices(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        List<Obd2Device> devices = deviceQueryService.getDevicesByTenant(tenantId);

        if (status != null && !status.isBlank()) {
            DeviceStatus targetStatus = DeviceStatus.valueOf(status.toUpperCase().trim());
            devices = devices.stream().filter(d -> d.getStatus() == targetStatus).toList();
        }

        return ResponseEntity.ok(devices.stream().map(deviceAssembler::toResponse).toList());
    }

    @Operation(summary = "Update operational status of an OBD-II device")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "409", description = "Device currently installed conflict")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('iot:devices:manage') or hasAnyRole('ROLE_HEAD_MECHANIC', 'ROLE_TENANT_ADMIN', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<Obd2DeviceResponse> updateDeviceStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeviceStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        DeviceId deviceId = DeviceId.of(id);

        Obd2Device device = deviceQueryService.handle(new GetDeviceByIdQuery(deviceId))
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        if (!device.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Device does not belong to the authenticated workshop");
        }

        DeviceStatus targetStatus = DeviceStatus.valueOf(request.status().toUpperCase().trim());
        deviceCommandService.handle(new UpdateDeviceStatusCommand(deviceId, targetStatus));

        Obd2Device updatedDevice = deviceQueryService.handle(new GetDeviceByIdQuery(deviceId))
                .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        return ResponseEntity.ok(deviceAssembler.toResponse(updatedDevice));
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new AccessDeniedException("Active tenant context is required");
        }
        return TenantId.of(userDetails.getTenantId());
    }
}
