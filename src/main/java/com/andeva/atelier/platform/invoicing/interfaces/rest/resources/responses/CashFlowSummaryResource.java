package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Presentation resource detailing the consolidated workshop cash flow summary
 * and chronological movements for a requested date range.
 *
 * @author Joel Huamani Estefanero
 */
public record CashFlowSummaryResource(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal accountsReceivable,
        BigDecimal netCashFlow,
        String currency,
        List<CashFlowMovementResource> movements
) implements Serializable {
}
