package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetPurchaseOrderDetailQuery(
        TenantId tenantId,
        PurchaseOrderId purchaseOrderId
) {
    public GetPurchaseOrderDetailQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(purchaseOrderId, "purchaseOrderId cannot be null");
    }
}
