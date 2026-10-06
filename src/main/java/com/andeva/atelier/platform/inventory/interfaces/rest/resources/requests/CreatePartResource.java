package com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests;

import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePartResource(
        @NotBlank(message = "Part name cannot be blank")
        String name,

        @NotBlank(message = "SKU cannot be blank")
        String sku,

        @NotNull(message = "Category is required")
        ItemCategory category,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.00", message = "Base price must be non-negative")
        BigDecimal basePrice,

        @NotNull(message = "Minimum stock is required")
        @DecimalMin(value = "0.00", message = "Minimum stock must be non-negative")
        BigDecimal minimumStock,

        String unitOfMeasure
) {
}
