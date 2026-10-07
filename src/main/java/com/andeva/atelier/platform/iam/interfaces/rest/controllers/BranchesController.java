package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.BranchCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.BranchQueryService;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateBranchCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateBranchLocationCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchesByTenantIdQuery;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.CreateBranchResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.UpdateBranchLocationResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.BranchResource;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.BranchResourceFromEntityAssembler;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller governing physical workshop branch establishments and GPS geofence configuration.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/branches")
@Tag(name = "Branches & Geofences", description = "Endpoints for managing workshop physical branches and GPS attendance perimeters")
public class BranchesController {

    private final BranchQueryService branchQueryService;
    private final BranchCommandService branchCommandService;

    public BranchesController(BranchQueryService branchQueryService, BranchCommandService branchCommandService) {
        this.branchQueryService = Objects.requireNonNull(branchQueryService, "BranchQueryService cannot be null");
        this.branchCommandService = Objects.requireNonNull(branchCommandService, "BranchCommandService cannot be null");
    }

    /**
     * Retrieves all operational physical branch locations belonging to the workshop in session.
     */
    @Operation(summary = "List all physical workshop branches for active tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of branches retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:branches:read")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('iam:branches:read')")
    public ResponseEntity<?> getBranches(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to access branch catalog"));
        }

        List<Branch> branches = branchQueryService.handle(new GetBranchesByTenantIdQuery(new TenantId(userDetails.getTenantId())));
        List<BranchResource> resources = branches.stream()
                .map(BranchResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    /**
     * Creates a new physical workshop branch location with GPS coordinates and circular attendance geofence.
     */
    @Operation(summary = "Create a new physical branch with GPS geofence")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Branch successfully created"),
            @ApiResponse(responseCode = "400", description = "Geodesic coordinate or radius validation failure"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:branches:manage or subscription quota exceeded")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('iam:branches:manage')")
    public ResponseEntity<?> createBranch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateBranchResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to create a branch"));
        }

        String sunatCode = (resource.sunatCode() != null && !resource.sunatCode().isBlank())
                ? resource.sunatCode()
                : "0000";

        CreateBranchCommand command = new CreateBranchCommand(
                new TenantId(userDetails.getTenantId()),
                resource.name(),
                sunatCode,
                GeoPoint.of(resource.latitude(), resource.longitude()),
                resource.geofenceRadiusMeters()
        );
        Result<Branch, ApplicationError> result = branchCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                BranchResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    /**
     * Retrieves individual branch details by its unique identifier.
     */
    @Operation(summary = "Get branch details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Branch found and returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:branches:read"),
            @ApiResponse(responseCode = "404", description = "Branch not found or belongs to another tenant")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('iam:branches:read')")
    public ResponseEntity<?> getBranchById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Optional<Branch> branchOptional = branchQueryService.handle(new GetBranchByIdQuery(BranchId.of(id)));
        if (branchOptional.isEmpty() || !branchOptional.get().tenantId().value().equals(userDetails.getTenantId())) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Branch not found with ID: " + id));
        }

        return ResponseEntity.ok(BranchResourceFromEntityAssembler.toResourceFromEntity(branchOptional.get()));
    }

    /**
     * Updates GPS coordinates and geofence radius of an existing workshop branch.
     */
    @Operation(summary = "Update branch GPS coordinates and attendance geofence radius")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Branch location updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation constraint failure on coordinates or radius"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:branches:manage"),
            @ApiResponse(responseCode = "404", description = "Branch not found or unauthorized")
    })
    @PutMapping({"/{id}", "/{id}/location"})
    @PreAuthorize("hasAuthority('iam:branches:manage')")
    public ResponseEntity<?> updateBranchLocation(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateBranchLocationResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Optional<Branch> branchOptional = branchQueryService.handle(new GetBranchByIdQuery(BranchId.of(id)));
        if (branchOptional.isEmpty() || !branchOptional.get().tenantId().value().equals(userDetails.getTenantId())) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Branch not found with ID: " + id));
        }

        Branch existingBranch = branchOptional.get();
        UpdateBranchLocationCommand command = new UpdateBranchLocationCommand(
                BranchId.of(id),
                existingBranch.name(),
                existingBranch.sunatCode(),
                GeoPoint.of(resource.latitude(), resource.longitude()),
                resource.geofenceRadiusMeters()
        );
        Result<Branch, ApplicationError> result = branchCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                BranchResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }
}
