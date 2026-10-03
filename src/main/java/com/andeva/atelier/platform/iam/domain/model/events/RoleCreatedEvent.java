package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a new security role is formulated or provisioned within a workshop Tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record RoleCreatedEvent(
        RoleId roleId,
        TenantId tenantId,
        String name,
        Instant occurredOn
) implements Serializable {

    public RoleCreatedEvent {
        Objects.requireNonNull(roleId, "Role identifier cannot be null");
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(name, "Role name cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static RoleCreatedEvent of(RoleId roleId, TenantId tenantId, String name) {
        return new RoleCreatedEvent(roleId, tenantId, name, Instant.now());
    }
}
