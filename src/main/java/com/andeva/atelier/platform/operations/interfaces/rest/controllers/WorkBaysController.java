package com.andeva.atelier.platform.operations.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.operations.application.commandservices.WorkBayCommandService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkBayQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateWorkBayCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.UpdateWorkBayStatusCommand;
import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetAvailableWorkBaysQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetWorkBaysByBranchIdQuery;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.CreateWorkBayResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.MaintenanceBayResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkBayResource;
import com.andeva.atelier.platform.operations.interfaces.rest.transform.CreateWorkBayCommandFromResourceAssembler;
import com.andeva.atelier.platform.operations.interfaces.rest.transform.WorkBayResourceAssembler;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/work-bays", "/api/v1/operations/bays", "/api/v1/operations/work-bays"})
@Tag(name = "Work Bays", description = "Endpoints for managing workshop physical work bays, lifts, and occupancy lifecycle")
public class WorkBaysController {

    private final WorkBayCommandService workBayCommandService;
    private final WorkBayQueryService workBayQueryService;

    public WorkBaysController(WorkBayCommandService workBayCommandService, WorkBayQueryService workBayQueryService) {
        this.workBayCommandService = Objects.requireNonNull(workBayCommandService, "workBayCommandService cannot be null");
        this.workBayQueryService = Objects.requireNonNull(workBayQueryService, "workBayQueryService cannot be null");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('operations:bays:write') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Register a new physical work bay in workshop branch")
    public ResponseEntity<?> createBay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateWorkBayResource resource,
            UriComponentsBuilder ucb
    ) {
        UUID tenantId = userDetails != null && userDetails.getTenantId() != null
                ? userDetails.getTenantId()
                : UUID.randomUUID();

        CreateWorkBayCommand command = CreateWorkBayCommandFromResourceAssembler.toCommandFromResource(tenantId, resource);
        Result<WorkBay, ApplicationError> result = workBayCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        WorkBay created = result.getOrThrow();
        WorkBayResource responseResource = WorkBayResourceAssembler.toResourceFromEntity(created);
        URI location = ucb.path("/api/v1/work-bays/{id}").buildAndExpand(created.getId().value()).toUri();
        return ResponseEntity.created(location).body(responseResource);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('operations:bays:read') or isAuthenticated()")
    @Operation(summary = "List work bays by branch with availability filtering")
    public ResponseEntity<?> getBays(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly
    ) {
        UUID tenantId = userDetails != null && userDetails.getTenantId() != null
                ? userDetails.getTenantId()
                : UUID.randomUUID();
        UUID targetBranchId = branchId != null ? branchId : UUID.randomUUID();

        List<WorkBay> bays = availableOnly
                ? workBayQueryService.handle(new GetAvailableWorkBaysQuery(new TenantId(tenantId), new BranchId(targetBranchId), null))
                : workBayQueryService.handle(new GetWorkBaysByBranchIdQuery(new TenantId(tenantId), new BranchId(targetBranchId)));

        List<WorkBayResource> resources = bays.stream()
                .map(WorkBayResourceAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{bayId}")
    @PreAuthorize("hasAuthority('operations:bays:read') or isAuthenticated()")
    @Operation(summary = "Get physical work bay details by id")
    public ResponseEntity<?> getBayById(@PathVariable UUID bayId) {
        return workBayQueryService.getById(new WorkBayId(bayId))
                .map(WorkBayResourceAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{bayId}/maintenance")
    @PreAuthorize("hasAuthority('operations:bays:write') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Place work bay under maintenance")
    public ResponseEntity<?> setMaintenance(
            @PathVariable UUID bayId,
            @Valid @RequestBody MaintenanceBayResource resource
    ) {
        UpdateWorkBayStatusCommand command = new UpdateWorkBayStatusCommand(
                new WorkBayId(bayId),
                BayStatus.MAINTENANCE,
                resource.reason()
        );
        Result<WorkBay, ApplicationError> result = workBayCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(WorkBayResourceAssembler.toResourceFromEntity(result.getOrThrow()));
    }

    @PostMapping("/{bayId}/restore")
    @PreAuthorize("hasAuthority('operations:bays:write') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Restore work bay to available status")
    public ResponseEntity<?> restoreAvailable(@PathVariable UUID bayId) {
        UpdateWorkBayStatusCommand command = new UpdateWorkBayStatusCommand(
                new WorkBayId(bayId),
                BayStatus.AVAILABLE,
                "Restored to service"
        );
        Result<WorkBay, ApplicationError> result = workBayCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(WorkBayResourceAssembler.toResourceFromEntity(result.getOrThrow()));
    }
}
