package com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.util.UUID;

public record BatchDeductionResource(
        UUID batchId,
        BigDecimal quantityDeducted,
        BigDecimal unitCost,
        BigDecimal subtotal
) {
}
