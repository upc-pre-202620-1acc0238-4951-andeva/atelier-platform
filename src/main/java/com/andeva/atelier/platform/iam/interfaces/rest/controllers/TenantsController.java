package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.TenantCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateTenantProfileCommand;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.UpdateTenantProfileResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.TenantResource;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.TenantResourceFromAggregateAssembler;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

/**
 * REST controller governing automotive workshop Tenant corporate profile and configurations.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenants", description = "Endpoints for managing workshop tenant profile and governance")
public class TenantsController {

    private final TenantQueryService tenantQueryService;
    private final TenantCommandService tenantCommandService;

    public TenantsController(TenantQueryService tenantQueryService, TenantCommandService tenantCommandService) {
        this.tenantQueryService = Objects.requireNonNull(tenantQueryService, "TenantQueryService cannot be null");
        this.tenantCommandService = Objects.requireNonNull(tenantCommandService, "TenantCommandService cannot be null");
    }

    /**
     * Retrieves corporate profile and tax metadata of the active workshop tenant resolved from authenticated session.
     */
    @Operation(summary = "Get corporate profile of the active workshop tenant in session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant profile successfully retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:tenants:read"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    @GetMapping("/current")
    @PreAuthorize("hasAuthority('iam:tenants:read')")
    public ResponseEntity<?> getCurrentTenant(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to access tenant profile"));
        }

        Optional<Tenant> tenantOptional = tenantQueryService.handle(new GetTenantByIdQuery(new TenantId(userDetails.getTenantId())));
        if (tenantOptional.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Active workshop tenant not found"));
        }

        TenantResource resource = TenantResourceFromAggregateAssembler.toResourceFromAggregate(tenantOptional.get());
        return ResponseEntity.ok(resource);
    }

    /**
     * Updates commercial and legal registered names of the active workshop tenant.
     */
    @Operation(summary = "Update corporate profile and business names of active workshop tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant profile successfully updated"),
            @ApiResponse(responseCode = "400", description = "Validation constraint failure on names"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:tenants:update")
    })
    @PutMapping("/current")
    @PreAuthorize("hasAuthority('iam:tenants:update')")
    public ResponseEntity<?> updateCurrentTenant(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateTenantProfileResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to update tenant profile"));
        }

        UpdateTenantProfileCommand command = new UpdateTenantProfileCommand(
                new TenantId(userDetails.getTenantId()),
                resource.name(),
                resource.legalName()
        );
        Result<Tenant, ApplicationError> result = tenantCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                TenantResourceFromAggregateAssembler::toResourceFromAggregate,
                HttpStatus.OK
        );
    }
}
