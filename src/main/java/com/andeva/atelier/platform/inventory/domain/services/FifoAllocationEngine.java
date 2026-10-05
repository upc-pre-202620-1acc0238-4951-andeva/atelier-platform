package com.andeva.atelier.platform.inventory.domain.services;

import com.andeva.atelier.platform.inventory.domain.exceptions.InsufficientStockException;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.BatchDeduction;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain service providing deterministic First-In, First-Out (FIFO) stock allocation.
 * Strictly consumes batches ordered by arrivalDate ASC and computes Cost of Goods Sold (COGS).
 */
public class FifoAllocationEngine {

    public StockAllocation allocate(List<InventoryBatch> batches, Quantity requestedQuantity, Currency defaultCurrency) {
        Objects.requireNonNull(batches, "batches cannot be null");
        Objects.requireNonNull(requestedQuantity, "requestedQuantity cannot be null");
        Currency currency = defaultCurrency != null ? defaultCurrency : Currency.PEN;

        if (requestedQuantity.isZero()) {
            throw new IllegalArgumentException("Requested quantity must be strictly greater than zero");
        }

        // Filter batches with remaining stock and sort by arrivalDate ASC
        List<InventoryBatch> availableBatches = batches.stream()
                .filter(InventoryBatch::hasStock)
                .sorted(Comparator.comparing(InventoryBatch::getArrivalDate))
                .toList();

        Quantity totalAvailable = availableBatches.stream()
                .map(InventoryBatch::getRemainingQuantity)
                .reduce(Quantity.ZERO, Quantity::add);

        if (totalAvailable.isLessThan(requestedQuantity)) {
            throw new InsufficientStockException("Insufficient stock in batches. Requested: "
                    + requestedQuantity.value() + ", Available: " + totalAvailable.value());
        }

        Quantity remainingToDeduct = requestedQuantity;
        List<BatchDeduction> deductions = new ArrayList<>();
        BigDecimal totalCogsAmount = BigDecimal.ZERO;

        for (InventoryBatch batch : availableBatches) {
            if (remainingToDeduct.isZero()) {
                break;
            }

            Quantity availableInBatch = batch.getRemainingQuantity();
            Quantity toDeductFromBatch;

            if (availableInBatch.isLessThanOrEqualTo(remainingToDeduct)) {
                toDeductFromBatch = availableInBatch;
            } else {
                toDeductFromBatch = remainingToDeduct;
            }

            batch.deduct(toDeductFromBatch);
            remainingToDeduct = remainingToDeduct.subtract(toDeductFromBatch);

            BatchDeduction deduction = new BatchDeduction(
                    batch.getId().value(),
                    toDeductFromBatch,
                    batch.getUnitCost()
            );
            deductions.add(deduction);
            totalCogsAmount = totalCogsAmount.add(deduction.subtotal().amount());
            currency = batch.getUnitCost().currency();
        }

        Money totalCogs = Money.of(totalCogsAmount, currency);
        return new StockAllocation(UUID.randomUUID(), requestedQuantity, totalCogs, deductions);
    }
}
