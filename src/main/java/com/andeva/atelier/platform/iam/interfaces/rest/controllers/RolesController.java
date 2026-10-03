package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.RoleCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.RoleQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateCustomRoleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.DeleteCustomRoleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetRoleToDefaultsCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateRolePermissionsCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.queries.GetAllPermissionsQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRoleByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRolesByTenantIdQuery;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.CreateRoleResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.UpdateRolePermissionsResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.PermissionResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.RoleResource;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.RoleResourceFromAggregateAssembler;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller governing RBAC security roles, sovereign permission assignments,
 * custom roles, and atomic platform privileges.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@Tag(name = "Roles & Permissions", description = "Endpoints for RBAC roles, tenant custom roles, and atomic platform permissions")
public class RolesController {

    private final RoleQueryService roleQueryService;
    private final RoleCommandService roleCommandService;

    public RolesController(RoleQueryService roleQueryService, RoleCommandService roleCommandService) {
        this.roleQueryService = Objects.requireNonNull(roleQueryService, "RoleQueryService cannot be null");
        this.roleCommandService = Objects.requireNonNull(roleCommandService, "RoleCommandService cannot be null");
    }

    /**
     * Lists all security roles (both factory system roles and custom roles) available in a tenant.
     */
    @Operation(summary = "List all security roles in a workshop tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of roles retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:roles:read")
    })
    @GetMapping("/api/v1/tenants/{tenantId}/roles")
    @PreAuthorize("hasAuthority('iam:roles:read')")
    public ResponseEntity<?> getRoles(
            @PathVariable UUID tenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (!isAuthorizedForTenant(tenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        List<Role> roles = roleQueryService.handle(new GetRolesByTenantIdQuery(TenantId.of(tenantId)));
        List<RoleResource> resources = roles.stream()
                .map(RoleResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();

        return ResponseEntity.ok(resources);
    }

    /**
     * Creates a bespoke custom role configured by the workshop tenant management.
     */
    @Operation(summary = "Create custom RBAC role for workshop tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Custom role created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation constraint failure"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:roles:create"),
            @ApiResponse(responseCode = "409", description = "Role name already exists in tenant")
    })
    @PostMapping("/api/v1/tenants/{tenantId}/roles")
    @PreAuthorize("hasAuthority('iam:roles:create')")
    public ResponseEntity<?> createRole(
            @PathVariable UUID tenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateRoleResource resource
    ) {
        if (!isAuthorizedForTenant(tenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        java.util.Set<PermissionId> permissionIds = resource.permissionIds().stream()
                .map(PermissionId::of)
                .collect(java.util.stream.Collectors.toSet());

        CreateCustomRoleCommand command = new CreateCustomRoleCommand(
                TenantId.of(tenantId),
                resource.name(),
                resource.description() != null ? resource.description() : "",
                permissionIds
        );
        Result<Role, ApplicationError> result = roleCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                RoleResourceFromAggregateAssembler::toResourceFromAggregate,
                HttpStatus.CREATED
        );
    }

    /**
     * Updates sovereignly the collection of permissions granted to a role.
     */
    @Operation(summary = "Update granted permissions for a tenant role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role permissions updated successfully"),
            @ApiResponse(responseCode = "400", description = "Empty permission list"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:roles:create"),
            @ApiResponse(responseCode = "404", description = "Role or permission not found")
    })
    @PutMapping("/api/v1/tenants/{tenantId}/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('iam:roles:create')")
    public ResponseEntity<?> updateRolePermissions(
            @PathVariable UUID tenantId,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateRolePermissionsResource resource
    ) {
        if (!isAuthorizedForTenant(tenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        Optional<Role> roleOptional = roleQueryService.handle(new GetRoleByIdQuery(RoleId.of(roleId)));
        if (roleOptional.isEmpty() || !roleOptional.get().tenantId().value().equals(tenantId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Role not found with ID: " + roleId));
        }

        java.util.Set<PermissionId> permissionIds = resource.permissionIds().stream()
                .map(PermissionId::of)
                .collect(java.util.stream.Collectors.toSet());

        UpdateRolePermissionsCommand command = new UpdateRolePermissionsCommand(
                RoleId.of(roleId),
                permissionIds
        );
        Result<Role, ApplicationError> result = roleCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                RoleResourceFromAggregateAssembler::toResourceFromAggregate,
                HttpStatus.OK
        );
    }

    /**
     * Restores a system template factory role to platform default recommended permissions.
     */
    @Operation(summary = "Reset system role to factory default permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role reset to defaults successfully"),
            @ApiResponse(responseCode = "400", description = "Role is not a factory system role"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:roles:create"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @PostMapping("/api/v1/tenants/{tenantId}/roles/{roleId}/reset-defaults")
    @PreAuthorize("hasAuthority('iam:roles:create')")
    public ResponseEntity<?> resetRoleToDefaults(
            @PathVariable UUID tenantId,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (!isAuthorizedForTenant(tenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        Optional<Role> roleOptional = roleQueryService.handle(new GetRoleByIdQuery(RoleId.of(roleId)));
        if (roleOptional.isEmpty() || !roleOptional.get().tenantId().value().equals(tenantId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Role not found with ID: " + roleId));
        }

        ResetRoleToDefaultsCommand command = new ResetRoleToDefaultsCommand(RoleId.of(roleId));
        Result<Role, ApplicationError> result = roleCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                RoleResourceFromAggregateAssembler::toResourceFromAggregate,
                HttpStatus.OK
        );
    }

    /**
     * Deletes an unused custom role from the workshop tenant.
     */
    @Operation(summary = "Delete an unused custom role")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Custom role deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:roles:create"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete system factory role or role in active use")
    })
    @DeleteMapping("/api/v1/tenants/{tenantId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('iam:roles:create')")
    public ResponseEntity<?> deleteRole(
            @PathVariable UUID tenantId,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (!isAuthorizedForTenant(tenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        Optional<Role> roleOptional = roleQueryService.handle(new GetRoleByIdQuery(RoleId.of(roleId)));
        if (roleOptional.isEmpty() || !roleOptional.get().tenantId().value().equals(tenantId)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Role not found with ID: " + roleId));
        }

        DeleteCustomRoleCommand command = new DeleteCustomRoleCommand(RoleId.of(roleId));
        Result<Void, ApplicationError> result = roleCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                unused -> null,
                HttpStatus.NO_CONTENT
        );
    }

    private boolean isAuthorizedForTenant(UUID tenantId, CustomUserDetails userDetails) {
        return userDetails != null && userDetails.getTenantId() != null && userDetails.getTenantId().equals(tenantId);
    }

    /**
     * Retrieves the entire immutable platform catalogue of atomic security privileges.
     */
    @Operation(summary = "List all atomic platform permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission catalogue retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:permissions:read")
    })
    @GetMapping("/api/v1/permissions")
    @PreAuthorize("hasAuthority('iam:permissions:read')")
    public ResponseEntity<?> getAllPermissions() {
        List<Permission> permissions = roleQueryService.handle(new GetAllPermissionsQuery());
        List<PermissionResource> resources = permissions.stream()
                .map(p -> new PermissionResource(p.id().value(), p.name(), p.description(), p.category()))
                .toList();

        return ResponseEntity.ok(resources);
    }
}
