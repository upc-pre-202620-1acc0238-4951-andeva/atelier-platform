package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a workshop Tenant transitions to ACTIVE status.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantActivatedEvent(
        TenantId tenantId,
        Instant occurredOn
) implements Serializable {

    public TenantActivatedEvent {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static TenantActivatedEvent of(TenantId tenantId) {
        return new TenantActivatedEvent(tenantId, Instant.now());
    }
}
