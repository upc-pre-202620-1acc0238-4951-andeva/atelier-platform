package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Internal profitability of a work order: parts sold at their fixed price against their FIFO cost (amounts before IGV).
 */
public record WorkOrderProfitabilityResource(
        UUID workOrderId,
        BigDecimal laborRevenue,
        BigDecimal partsRevenue,
        BigDecimal partsCostOfSales,
        BigDecimal partsGrossProfit,
        BigDecimal subtotal,
        String currency,
        List<PartProfitabilityResource> lines
) {
}
