package com.andeva.atelier.platform.inventory.interfaces.acl.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Audit view of a batch deduction charged to a work order and of the units returned since (US33 E2).
 */
public record PartMovementDto(
        UUID allocationId,
        UUID itemId,
        UUID taskId,
        UUID batchId,
        BigDecimal chargedQuantity,
        BigDecimal returnedQuantity,
        BigDecimal unitCost,
        String currency,
        Instant chargedAt,
        Instant lastReturnedAt,
        UUID lastReturnedBy,
        String lastReturnReason
) {
}
