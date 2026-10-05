package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;

public record IssuePurchaseOrderCommand(
        TenantId tenantId,
        PurchaseOrderId purchaseOrderId
) implements Serializable {

    public IssuePurchaseOrderCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(purchaseOrderId, "purchaseOrderId cannot be null");
    }
}
