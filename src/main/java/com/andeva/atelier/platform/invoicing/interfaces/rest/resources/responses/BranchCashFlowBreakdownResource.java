package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Per-branch fiscal and cash flow totals included in corporate multi-branch reports.
 */
public record BranchCashFlowBreakdownResource(
        UUID branchId,
        String branchName,
        BigDecimal laborRevenue,
        BigDecimal partsRevenue,
        BigDecimal igvCollected,
        BigDecimal totalRevenue,
        String currency
) implements Serializable {
}
