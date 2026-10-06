package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record PurchaseOrderCanceledEvent(
        PurchaseOrderId orderId,
        TenantId tenantId,
        String reason,
        Instant occurredOn
) implements Serializable {

    public PurchaseOrderCanceledEvent {
        Objects.requireNonNull(orderId, "orderId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static PurchaseOrderCanceledEvent of(PurchaseOrderId orderId, TenantId tenantId, String reason) {
        return new PurchaseOrderCanceledEvent(orderId, tenantId, reason, Instant.now());
    }
}
