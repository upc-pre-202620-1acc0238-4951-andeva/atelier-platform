package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.MembershipCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.MembershipQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.UserQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AssignRolesToMembershipCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.DeactivateMembershipCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateMembershipCompensationCommand;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipsByTenantIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.AssignRolesResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.UpdateCompensationResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.MembershipResource;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.MembershipResourceFromAggregateAssembler;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller governing staff employment memberships, remuneration schemes,
 * and RBAC role assignments within a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping({"/api/v1/memberships", "/api/v1/tenants/{tenantId}/memberships"})
@Tag(name = "Memberships", description = "Endpoints for staff memberships, compensation, and role assignments")
public class MembershipsController {

    private final MembershipQueryService membershipQueryService;
    private final MembershipCommandService membershipCommandService;
    private final UserQueryService userQueryService;

    public MembershipsController(
            MembershipQueryService membershipQueryService,
            MembershipCommandService membershipCommandService,
            UserQueryService userQueryService
    ) {
        this.membershipQueryService = Objects.requireNonNull(membershipQueryService, "MembershipQueryService cannot be null");
        this.membershipCommandService = Objects.requireNonNull(membershipCommandService, "MembershipCommandService cannot be null");
        this.userQueryService = Objects.requireNonNull(userQueryService, "UserQueryService cannot be null");
    }

    /**
     * Lists all staff memberships affiliated with the specified workshop tenant.
     */
    @Operation(summary = "List all staff memberships for a tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of memberships retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:members:read")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('iam:members:read')")
    public ResponseEntity<?> getMemberships(
            @PathVariable(value = "tenantId", required = false) UUID tenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID effectiveTenantId = resolveEffectiveTenantId(tenantId, userDetails);
        if (effectiveTenantId == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to access memberships"));
        }
        if (!isAuthorizedForTenant(effectiveTenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        List<TenantMembership> memberships = membershipQueryService.handle(new GetMembershipsByTenantIdQuery(TenantId.of(effectiveTenantId)));

        List<MembershipResource> resources = memberships.stream()
                .map(this::toMembershipResource)
                .toList();

        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves individual staff membership details by ID within a tenant.
     */
    @Operation(summary = "Get staff membership details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership details found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:members:read or cross-tenant access"),
            @ApiResponse(responseCode = "404", description = "Membership not found or belongs to another tenant")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('iam:members:read')")
    public ResponseEntity<?> getMembershipById(
            @PathVariable(value = "tenantId", required = false) UUID tenantId,
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID effectiveTenantId = resolveEffectiveTenantId(tenantId, userDetails);
        if (effectiveTenantId == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to access membership"));
        }
        if (!isAuthorizedForTenant(effectiveTenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        Optional<TenantMembership> membershipOptional = membershipQueryService.handle(new GetMembershipByIdQuery(TenantMembershipId.of(id)));
        if (membershipOptional.isEmpty() || !membershipOptional.get().tenantId().value().equals(effectiveTenantId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Membership not found with ID: " + id));
        }

        return ResponseEntity.ok(toMembershipResource(membershipOptional.get()));
    }

    /**
     * Updates assigned RBAC security roles for a staff membership.
     */
    @Operation(summary = "Assign security roles to a staff membership")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Empty role ID list"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:members:manage_roles or cross-tenant access"),
            @ApiResponse(responseCode = "404", description = "Membership or Role not found")
    })
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('iam:members:manage_roles')")
    public ResponseEntity<?> assignRoles(
            @PathVariable(value = "tenantId", required = false) UUID tenantId,
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AssignRolesResource resource
    ) {
        UUID effectiveTenantId = resolveEffectiveTenantId(tenantId, userDetails);
        if (effectiveTenantId == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to assign roles"));
        }
        if (!isAuthorizedForTenant(effectiveTenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        Optional<TenantMembership> membershipOptional = membershipQueryService.handle(new GetMembershipByIdQuery(TenantMembershipId.of(id)));
        if (membershipOptional.isEmpty() || !membershipOptional.get().tenantId().value().equals(effectiveTenantId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Membership not found with ID: " + id));
        }

        java.util.Set<RoleId> roleIds = resource.roleIds().stream()
                .map(RoleId::of)
                .collect(java.util.stream.Collectors.toSet());

        AssignRolesToMembershipCommand command = new AssignRolesToMembershipCommand(
                TenantMembershipId.of(id),
                roleIds
        );
        Result<TenantMembership, ApplicationError> result = membershipCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                this::toMembershipResource,
                HttpStatus.OK
        );
    }

    /**
     * Updates contractual salary compensation scheme and currency for a staff member.
     */
    @Operation(summary = "Update salary compensation for a staff membership")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compensation scheme updated"),
            @ApiResponse(responseCode = "400", description = "Invalid salary type or negative amount"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:members:compensate or cross-tenant access"),
            @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PutMapping("/{id}/compensation")
    @PreAuthorize("hasAuthority('iam:members:compensate')")
    public ResponseEntity<?> updateCompensation(
            @PathVariable(value = "tenantId", required = false) UUID tenantId,
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateCompensationResource resource
    ) {
        UUID effectiveTenantId = resolveEffectiveTenantId(tenantId, userDetails);
        if (effectiveTenantId == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to update compensation"));
        }
        if (!isAuthorizedForTenant(effectiveTenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        Optional<TenantMembership> membershipOptional = membershipQueryService.handle(new GetMembershipByIdQuery(TenantMembershipId.of(id)));
        if (membershipOptional.isEmpty() || !membershipOptional.get().tenantId().value().equals(effectiveTenantId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Membership not found with ID: " + id));
        }

        SalaryType salaryType;
        Currency currency;
        try {
            salaryType = SalaryType.valueOf(resource.salaryType().trim().toUpperCase());
            currency = Currency.valueOf(resource.currency().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.badRequest("Invalid salary type or currency: " + e.getMessage()));
        }

        UpdateMembershipCompensationCommand command = new UpdateMembershipCompensationCommand(
                TenantMembershipId.of(id),
                salaryType,
                Money.of(resource.baseSalary(), currency)
        );
        Result<TenantMembership, ApplicationError> result = membershipCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                this::toMembershipResource,
                HttpStatus.OK
        );
    }

    /**
     * Deactivates employment membership for a collaborator within the workshop.
     */
    @Operation(summary = "Deactivate staff membership")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membership deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:members:manage_roles or cross-tenant access"),
            @ApiResponse(responseCode = "404", description = "Membership not found"),
            @ApiResponse(responseCode = "409", description = "Cannot deactivate last workshop owner")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('iam:members:manage_roles')")
    public ResponseEntity<?> deactivateMembership(
            @PathVariable(value = "tenantId", required = false) UUID tenantId,
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID effectiveTenantId = resolveEffectiveTenantId(tenantId, userDetails);
        if (effectiveTenantId == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to deactivate membership"));
        }
        if (!isAuthorizedForTenant(effectiveTenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        Optional<TenantMembership> membershipOptional = membershipQueryService.handle(new GetMembershipByIdQuery(TenantMembershipId.of(id)));
        if (membershipOptional.isEmpty() || !membershipOptional.get().tenantId().value().equals(effectiveTenantId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Membership not found with ID: " + id));
        }

        DeactivateMembershipCommand command = new DeactivateMembershipCommand(TenantMembershipId.of(id));
        Result<Void, ApplicationError> result = membershipCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                unused -> null,
                HttpStatus.NO_CONTENT
        );
    }

    private UUID resolveEffectiveTenantId(UUID tenantId, CustomUserDetails userDetails) {
        if (tenantId != null) {
            return tenantId;
        }
        return userDetails != null ? userDetails.getTenantId() : null;
    }

    private boolean isAuthorizedForTenant(UUID tenantId, CustomUserDetails userDetails) {
        return userDetails != null && userDetails.getTenantId() != null && userDetails.getTenantId().equals(tenantId);
    }

    private MembershipResource toMembershipResource(TenantMembership membership) {
        User user = userQueryService.handle(new GetUserByIdQuery(membership.userId())).orElse(null);
        return MembershipResourceFromAggregateAssembler.toResourceFromAggregate(membership, user);
    }
}
