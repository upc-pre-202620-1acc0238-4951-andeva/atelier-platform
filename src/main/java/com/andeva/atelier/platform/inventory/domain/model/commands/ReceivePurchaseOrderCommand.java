package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record ReceivePurchaseOrderCommand(
        TenantId tenantId,
        PurchaseOrderId purchaseOrderId,
        StorageUrl receiptImageUrl,
        String receiptNumber,
        Instant receivedAt
) implements Serializable {

    public ReceivePurchaseOrderCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(purchaseOrderId, "purchaseOrderId cannot be null");
        Objects.requireNonNull(receiptImageUrl, "receiptImageUrl cannot be null");
        Objects.requireNonNull(receiptNumber, "receiptNumber cannot be null");
    }
}
