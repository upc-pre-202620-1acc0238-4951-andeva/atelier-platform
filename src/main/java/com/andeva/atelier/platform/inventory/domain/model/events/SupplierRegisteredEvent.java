package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record SupplierRegisteredEvent(
        SupplierId supplierId,
        TenantId tenantId,
        String businessName,
        TaxId taxId,
        Instant occurredOn
) implements Serializable {

    public SupplierRegisteredEvent {
        Objects.requireNonNull(supplierId, "supplierId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(businessName, "businessName cannot be null");
        Objects.requireNonNull(taxId, "taxId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static SupplierRegisteredEvent of(SupplierId supplierId, TenantId tenantId, String businessName, TaxId taxId) {
        return new SupplierRegisteredEvent(supplierId, tenantId, businessName, taxId, Instant.now());
    }
}
