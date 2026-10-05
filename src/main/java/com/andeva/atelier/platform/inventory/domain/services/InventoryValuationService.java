package com.andeva.atelier.platform.inventory.domain.services;

import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Domain service that computes inventory asset valuation under FIFO.
 */
public class InventoryValuationService {

    public Money calculateValuation(List<InventoryBatch> batches, Currency defaultCurrency) {
        Objects.requireNonNull(batches, "batches cannot be null");
        Currency currency = defaultCurrency != null ? defaultCurrency : Currency.PEN;

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (InventoryBatch batch : batches) {
            if (batch.hasStock()) {
                BigDecimal batchValue = batch.getRemainingQuantity().value()
                        .multiply(batch.getUnitCost().amount());
                totalAmount = totalAmount.add(batchValue);
                currency = batch.getUnitCost().currency();
            }
        }
        return Money.of(totalAmount, currency);
    }
}
