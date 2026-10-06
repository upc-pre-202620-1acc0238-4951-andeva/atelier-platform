package com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreatePurchaseOrderResource(
        @NotNull(message = "Supplier ID is required")
        UUID supplierId,

        @NotNull(message = "Branch ID is required")
        UUID branchId,

        String orderNumber,
        String notes,
        LocalDate expectedDeliveryDate,

        @NotEmpty(message = "Items list cannot be empty")
        @Valid
        List<CreatePurchaseOrderItemResource> items
) {
}
