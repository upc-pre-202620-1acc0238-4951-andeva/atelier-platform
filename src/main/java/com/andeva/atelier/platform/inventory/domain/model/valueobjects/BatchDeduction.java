package com.andeva.atelier.platform.inventory.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

public record BatchDeduction(
        UUID batchId,
        Quantity quantityDeducted,
        Money unitCost
) implements Serializable {

    public BatchDeduction {
        Objects.requireNonNull(batchId, "batchId cannot be null");
        Objects.requireNonNull(quantityDeducted, "quantityDeducted cannot be null");
        Objects.requireNonNull(unitCost, "unitCost cannot be null");
    }

    public Money subtotal() {
        BigDecimal total = unitCost.amount().multiply(quantityDeducted.value()).setScale(2, RoundingMode.HALF_EVEN);
        return Money.of(total, unitCost.currency());
    }
}
