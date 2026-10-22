package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Outbound port to Inventory: where the parts charged to a work order came from, what they cost and what was returned.
 */
public interface WorkOrderPartLedgerAclService {

    /** FIFO cost of the units still charged to the order. */
    List<PartCost> costsOf(UUID workOrderId);

    /** Charges and returns of the order (audit trail). */
    List<PartMovement> movementsOf(UUID workOrderId);

    record PartCost(UUID itemId, UUID taskId, UUID batchId, BigDecimal quantity, BigDecimal unitCost,
                    BigDecimal totalCost, String currency) {
    }

    record PartMovement(UUID allocationId, UUID itemId, UUID taskId, UUID batchId, BigDecimal chargedQuantity,
                        BigDecimal returnedQuantity, BigDecimal unitCost, String currency, Instant chargedAt,
                        Instant lastReturnedAt, UUID lastReturnedBy, String lastReturnReason) {
    }
}
