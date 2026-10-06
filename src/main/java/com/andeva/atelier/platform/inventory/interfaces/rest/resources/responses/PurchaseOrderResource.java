package com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses;

import com.andeva.atelier.platform.inventory.domain.model.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResource(
        UUID id,
        UUID tenantId,
        UUID supplierId,
        UUID branchId,
        String orderNumber,
        PurchaseOrderStatus status,
        BigDecimal totalCost,
        String receiptImageUrl,
        String receiptNumber,
        Instant receivedAt,
        List<PurchaseOrderItemResource> items
) {
}
