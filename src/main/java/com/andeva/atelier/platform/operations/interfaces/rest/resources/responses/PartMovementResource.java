package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A batch deduction charged to a task and the units returned since: who returned them, when and why.
 *
 * @param lastReturnedBy user id of whoever withdrew the part from the task
 */
public record PartMovementResource(
        UUID allocationId,
        UUID productId,
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
