package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record PurchaseOrderCreatedEvent(
        PurchaseOrderId orderId,
        TenantId tenantId,
        SupplierId supplierId,
        Instant occurredOn
) implements Serializable {

    public PurchaseOrderCreatedEvent {
        Objects.requireNonNull(orderId, "orderId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(supplierId, "supplierId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static PurchaseOrderCreatedEvent of(PurchaseOrderId orderId, TenantId tenantId, SupplierId supplierId) {
        return new PurchaseOrderCreatedEvent(orderId, tenantId, supplierId, Instant.now());
    }
}
