package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a workshop Tenant is suspended.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantSuspendedEvent(
        TenantId tenantId,
        String reason,
        Instant occurredOn
) implements Serializable {

    public TenantSuspendedEvent {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(reason, "Suspension reason cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static TenantSuspendedEvent of(TenantId tenantId, String reason) {
        return new TenantSuspendedEvent(tenantId, reason, Instant.now());
    }
}
