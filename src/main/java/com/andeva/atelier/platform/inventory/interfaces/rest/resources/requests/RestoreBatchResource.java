package com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RestoreBatchResource(
        @NotNull(message = "Item ID is required")
        UUID itemId,

        UUID batchId,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
        BigDecimal quantity,

        UUID workOrderId,
        String reason
) {
}
