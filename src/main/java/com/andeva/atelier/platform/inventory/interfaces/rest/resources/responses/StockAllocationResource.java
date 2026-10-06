package com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record StockAllocationResource(
        UUID allocationId,
        BigDecimal allocatedQuantity,
        BigDecimal totalCogs,
        List<BatchDeductionResource> deductions
) {
}
