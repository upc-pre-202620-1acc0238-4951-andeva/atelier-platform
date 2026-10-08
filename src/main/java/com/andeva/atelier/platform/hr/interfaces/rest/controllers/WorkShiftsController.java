package com.andeva.atelier.platform.hr.interfaces.rest.controllers;

import com.andeva.atelier.platform.hr.application.commandservices.WorkShiftCommandService;
import com.andeva.atelier.platform.hr.application.queryservices.WorkShiftQueryService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.commands.ActivateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.CreateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.DeactivateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.model.queries.GetWorkShiftByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListWorkShiftsByTenantQuery;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.CreateWorkShiftResource;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.UpdateWorkShiftResource;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.WorkShiftResource;
import com.andeva.atelier.platform.hr.interfaces.rest.transform.WorkShiftResourceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/hr/work-shifts")
@Tag(name = "Work Shifts", description = "Endpoints for managing workshop work shifts and schedules")
public class WorkShiftsController {

    private final WorkShiftCommandService workShiftCommandService;
    private final WorkShiftQueryService workShiftQueryService;

    public WorkShiftsController(
            WorkShiftCommandService workShiftCommandService,
            WorkShiftQueryService workShiftQueryService
    ) {
        this.workShiftCommandService = Objects.requireNonNull(workShiftCommandService, "workShiftCommandService cannot be null");
        this.workShiftQueryService = Objects.requireNonNull(workShiftQueryService, "workShiftQueryService cannot be null");
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails, UUID tenantHeader) {
        if (tenantHeader != null) {
            if (userDetails != null && userDetails.getTenantId() != null && !userDetails.getTenantId().equals(tenantHeader)) {
                throw new org.springframework.security.access.AccessDeniedException("Tenant ID in header does not match authenticated user context");
            }
            return TenantId.of(tenantHeader);
        }
        if (userDetails != null && userDetails.getTenantId() != null) {
            return TenantId.of(userDetails.getTenantId());
        }
        throw new org.springframework.security.access.AccessDeniedException("Active tenant context is required");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('hr:shifts:manage') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Create a new work shift")
    public ResponseEntity<?> createWorkShift(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @Valid @RequestBody CreateWorkShiftResource resource,
            UriComponentsBuilder uriBuilder
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        CreateWorkShiftCommand command = WorkShiftResourceAssembler.toCommand(tenantId, resource);
        Result<WorkShift, ApplicationError> result = workShiftCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        WorkShift created = result.getOrThrow();
        URI location = uriBuilder.path("/api/v1/hr/work-shifts/{shiftId}")
                .buildAndExpand(created.getId().value()).toUri();
        return ResponseEntity.created(location).body(WorkShiftResourceAssembler.toResource(created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('hr:shifts:read') or hasAuthority('hr:shifts:manage') or isAuthenticated()")
    @Operation(summary = "List all work shifts")
    public ResponseEntity<List<WorkShiftResource>> listWorkShifts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        List<WorkShift> list = workShiftQueryService.handle(new ListWorkShiftsByTenantQuery(tenantId));
        return ResponseEntity.ok(WorkShiftResourceAssembler.toResourceList(list));
    }

    @GetMapping("/{shiftId}")
    @PreAuthorize("hasAuthority('hr:shifts:read') or hasAuthority('hr:shifts:manage') or isAuthenticated()")
    @Operation(summary = "Get work shift by ID")
    public ResponseEntity<?> getWorkShiftById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID shiftId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        Optional<WorkShift> shiftOpt = workShiftQueryService.handle(
                new GetWorkShiftByIdQuery(tenantId, WorkShiftId.of(shiftId))
        );
        if (shiftOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(WorkShiftResourceAssembler.toResource(shiftOpt.get()));
    }

    @PutMapping("/{shiftId}")
    @PreAuthorize("hasAuthority('hr:shifts:manage') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Update an existing work shift")
    public ResponseEntity<?> updateWorkShift(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID shiftId,
            @Valid @RequestBody UpdateWorkShiftResource resource
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        UpdateWorkShiftCommand command = WorkShiftResourceAssembler.toCommand(tenantId, WorkShiftId.of(shiftId), resource);
        Result<WorkShift, ApplicationError> result = workShiftCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.ok(WorkShiftResourceAssembler.toResource(result.getOrThrow()));
    }

    @PatchMapping("/{shiftId}/activate")
    @PreAuthorize("hasAuthority('hr:shifts:manage') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Activate a work shift")
    public ResponseEntity<?> activateWorkShift(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID shiftId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        Result<Void, ApplicationError> result = workShiftCommandService.handle(
                new ActivateWorkShiftCommand(tenantId, WorkShiftId.of(shiftId))
        );
        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{shiftId}/deactivate")
    @PreAuthorize("hasAuthority('hr:shifts:manage') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Deactivate a work shift")
    public ResponseEntity<?> deactivateWorkShift(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID shiftId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        Result<Void, ApplicationError> result = workShiftCommandService.handle(
                new DeactivateWorkShiftCommand(tenantId, WorkShiftId.of(shiftId))
        );
        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.noContent().build();
    }
}
