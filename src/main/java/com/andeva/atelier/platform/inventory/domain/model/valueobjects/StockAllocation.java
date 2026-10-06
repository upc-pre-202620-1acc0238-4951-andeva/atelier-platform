package com.andeva.atelier.platform.inventory.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record StockAllocation(
        UUID allocationId,
        Quantity allocatedQuantity,
        Money totalCostOfGoodsSold,
        List<BatchDeduction> deductions
) implements Serializable {

    public StockAllocation {
        Objects.requireNonNull(allocationId, "allocationId cannot be null");
        Objects.requireNonNull(allocatedQuantity, "allocatedQuantity cannot be null");
        Objects.requireNonNull(totalCostOfGoodsSold, "totalCostOfGoodsSold cannot be null");
        deductions = deductions != null ? Collections.unmodifiableList(deductions) : List.of();
    }

    public static StockAllocation of(Quantity allocatedQuantity, Money totalCostOfGoodsSold, List<BatchDeduction> deductions) {
        return new StockAllocation(UUID.randomUUID(), allocatedQuantity, totalCostOfGoodsSold, deductions);
    }

    public Money totalCogs() {
        return totalCostOfGoodsSold;
    }
}
