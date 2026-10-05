package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record PurchaseOrderReceivedEvent(
        PurchaseOrderId orderId,
        TenantId tenantId,
        SupplierId supplierId,
        Money totalCost,
        Instant occurredOn
) implements Serializable {

    public PurchaseOrderReceivedEvent {
        Objects.requireNonNull(orderId, "orderId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(supplierId, "supplierId cannot be null");
        Objects.requireNonNull(totalCost, "totalCost cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static PurchaseOrderReceivedEvent of(PurchaseOrderId orderId, TenantId tenantId, SupplierId supplierId, Money totalCost) {
        return new PurchaseOrderReceivedEvent(orderId, tenantId, supplierId, totalCost, Instant.now());
    }
}
