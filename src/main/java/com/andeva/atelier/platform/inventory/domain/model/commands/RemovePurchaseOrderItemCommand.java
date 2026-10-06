package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderItemId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;

public record RemovePurchaseOrderItemCommand(
        TenantId tenantId,
        PurchaseOrderId purchaseOrderId,
        PurchaseOrderItemId purchaseOrderItemId
) implements Serializable {

    public RemovePurchaseOrderItemCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(purchaseOrderId, "purchaseOrderId cannot be null");
        Objects.requireNonNull(purchaseOrderItemId, "purchaseOrderItemId cannot be null");
    }
}
