package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.events.RoleCreatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.RoleUpdatedEvent;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Sovereign RBAC security role aggregate root scoped strictly to a workshop Tenant.
 *
 * @author Joel Huamani Estefanero
 */
public class Role extends AbstractDomainAggregateRoot<Role> {

    private final RoleId id;
    private final TenantId tenantId;
    private final String code;
    private String name;
    private String description;
    private final boolean systemRole;
    private final Set<Permission> permissions;

    public Role(
            RoleId id,
            TenantId tenantId,
            String code,
            String name,
            String description,
            boolean systemRole,
            Set<Permission> permissions) {
        this.id = Objects.requireNonNull(id, "Role identifier cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        this.code = code;
        this.name = validateName(name);
        this.description = Objects.requireNonNull(description, "Role description cannot be null").trim();
        this.systemRole = systemRole;
        this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }

    /**
     * Domain factory to instantiate a bespoke custom role formulated by workshop administration.
     */
    public static Role createCustom(
            TenantId tenantId,
            String name,
            String description,
            Set<Permission> permissions) {
        RoleId roleId = RoleId.generate();
        Role role = new Role(
                roleId,
                tenantId,
                null,
                name,
                description,
                false,
                permissions
        );
        role.registerEvent(RoleCreatedEvent.of(roleId, tenantId, name));
        return role;
    }

    /**
     * Domain factory to provision an initial factory template role during tenant onboarding.
     */
    public static Role provisionFromTemplate(
            TenantId tenantId,
            RoleTemplate template,
            Set<Permission> templatePermissions) {
        Objects.requireNonNull(template, "Role template cannot be null");
        RoleId roleId = RoleId.generate();
        Role role = new Role(
                roleId,
                tenantId,
                template.code(),
                template.defaultName(),
                template.description(),
                true,
                templatePermissions
        );
        role.registerEvent(RoleCreatedEvent.of(roleId, tenantId, template.defaultName()));
        return role;
    }

    public static Role fromTemplate(
            TenantId tenantId,
            RoleTemplate template,
            Set<Permission> templatePermissions) {
        return provisionFromTemplate(tenantId, template, templatePermissions);
    }

    /**
     * Overwrites permissions assigned to this role sovereignly and registers a RoleUpdatedEvent.
     */
    public void updatePermissions(Set<Permission> newPermissions) {
        Objects.requireNonNull(newPermissions, "New permissions set cannot be null");
        this.permissions.clear();
        this.permissions.addAll(newPermissions);
        registerEvent(RoleUpdatedEvent.of(this.id, this.tenantId, this.name));
    }

    /**
     * Resets a system role's permissions back to the platform factory template defaults.
     */
    public void resetToTemplate(RoleTemplate template, Set<Permission> templatePermissions) {
        if (!this.systemRole) {
            throw new IllegalStateException("Only protected system roles can be reset to template");
        }
        Objects.requireNonNull(template, "Role template cannot be null");
        Objects.requireNonNull(templatePermissions, "Template permissions cannot be null");
        this.name = template.defaultName();
        this.description = template.description();
        this.permissions.clear();
        this.permissions.addAll(templatePermissions);
        registerEvent(RoleUpdatedEvent.of(this.id, this.tenantId, this.name));
    }

    public void grantPermission(Permission permission) {
        Objects.requireNonNull(permission, "Permission to grant cannot be null");
        this.permissions.add(permission);
    }

    public void revokePermission(PermissionId permissionId) {
        Objects.requireNonNull(permissionId, "Permission identifier to revoke cannot be null");
        this.permissions.removeIf(p -> p.id().equals(permissionId));
    }

    public void updateDetails(String name, String description) {
        this.name = validateName(name);
        this.description = Objects.requireNonNull(description, "Description cannot be null").trim();
    }

    public RoleId id() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public Set<Permission> permissions() {
        return Collections.unmodifiableSet(permissions);
    }

    private static String validateName(String name) {
        Objects.requireNonNull(name, "Role name cannot be null");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        if (trimmed.length() > 50) {
            throw new IllegalArgumentException("Role name cannot exceed 50 characters");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
