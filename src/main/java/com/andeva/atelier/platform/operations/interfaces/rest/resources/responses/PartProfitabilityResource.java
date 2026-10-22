package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One part of one task: units, sale price, FIFO cost of sales and gross profit.
 */
public record PartProfitabilityResource(
        UUID taskId,
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal revenue,
        BigDecimal costOfSales,
        BigDecimal grossProfit
) {
}
