package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;

import java.util.Objects;

public record GetPurchaseOrderByIdQuery(
        PurchaseOrderId purchaseOrderId
) {
    public GetPurchaseOrderByIdQuery {
        Objects.requireNonNull(purchaseOrderId, "purchaseOrderId cannot be null");
    }
}
