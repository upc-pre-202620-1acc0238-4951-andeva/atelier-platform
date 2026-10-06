package com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReceiveBatchResource(
        @NotNull(message = "Item ID is required")
        UUID itemId,

        UUID supplierId,
        UUID purchaseOrderId,

        @NotBlank(message = "Batch number is required")
        String batchNumber,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
        BigDecimal quantity,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.00", message = "Unit cost must be non-negative")
        BigDecimal unitCost,

        Instant arrivalDate,
        String receiptImageUrl
) {
}
