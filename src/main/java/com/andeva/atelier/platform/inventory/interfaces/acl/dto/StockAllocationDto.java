package com.andeva.atelier.platform.inventory.interfaces.acl.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockAllocationDto(
        UUID allocationId,
        UUID itemId,
        BigDecimal allocatedQuantity,
        BigDecimal totalCogs
) {
}
