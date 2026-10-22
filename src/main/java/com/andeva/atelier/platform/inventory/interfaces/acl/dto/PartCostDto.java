package com.andeva.atelier.platform.inventory.interfaces.acl.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Units of one batch still charged to a work order task, at their FIFO acquisition cost.
 */
public record PartCostDto(
        UUID itemId,
        UUID taskId,
        UUID batchId,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalCost,
        String currency
) {
}
