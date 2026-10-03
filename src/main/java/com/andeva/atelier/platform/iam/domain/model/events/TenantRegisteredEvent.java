package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a new automotive workshop Tenant is successfully registered.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantRegisteredEvent(
        TenantId tenantId,
        String name,
        TaxId taxId,
        Instant occurredOn
) implements Serializable {

    public TenantRegisteredEvent {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(name, "Tenant name cannot be null");
        Objects.requireNonNull(taxId, "Tax ID cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static TenantRegisteredEvent of(TenantId tenantId, String name, TaxId taxId) {
        return new TenantRegisteredEvent(tenantId, name, taxId, Instant.now());
    }
}
