package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a security role's metadata or permissions set is updated.
 *
 * @author Joel Huamani Estefanero
 */
public record RoleUpdatedEvent(
        RoleId roleId,
        TenantId tenantId,
        String name,
        Instant occurredOn
) implements Serializable {

    public RoleUpdatedEvent {
        Objects.requireNonNull(roleId, "Role identifier cannot be null");
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(name, "Role name cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static RoleUpdatedEvent of(RoleId roleId, TenantId tenantId, String name) {
        return new RoleUpdatedEvent(roleId, tenantId, name, Instant.now());
    }
}
