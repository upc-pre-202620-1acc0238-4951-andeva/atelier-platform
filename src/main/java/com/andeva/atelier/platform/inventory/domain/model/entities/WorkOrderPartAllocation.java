package com.andeva.atelier.platform.inventory.domain.model.entities;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Units of one batch charged to a work order task (one FIFO deduction), with their acquisition cost.
 * It is the trail of where every charged unit came from: returns go back to this batch (US33) and its
 * cost is the order's cost of sales (US35).
 */
public class WorkOrderPartAllocation {

    private final UUID id;
    private final TenantId tenantId;
    private final InventoryItemId itemId;
    private final UUID batchId;
    private final UUID workOrderId;
    private final UUID taskId;
    private final BigDecimal quantity;
    private final Money unitCost;
    private final Instant allocatedAt;
    private BigDecimal returnedQuantity;
    private Instant lastReturnedAt;
    private UUID lastReturnedBy;
    private String lastReturnReason;

    public WorkOrderPartAllocation(UUID id, TenantId tenantId, InventoryItemId itemId, UUID batchId, UUID workOrderId,
                                   UUID taskId, BigDecimal quantity, Money unitCost, Instant allocatedAt,
                                   BigDecimal returnedQuantity, Instant lastReturnedAt, UUID lastReturnedBy,
                                   String lastReturnReason) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId cannot be null");
        this.itemId = Objects.requireNonNull(itemId, "itemId cannot be null");
        this.batchId = Objects.requireNonNull(batchId, "batchId cannot be null");
        this.workOrderId = Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        this.taskId = taskId;
        this.quantity = Objects.requireNonNull(quantity, "quantity cannot be null");
        this.unitCost = Objects.requireNonNull(unitCost, "unitCost cannot be null");
        this.allocatedAt = Objects.requireNonNull(allocatedAt, "allocatedAt cannot be null");
        this.returnedQuantity = returnedQuantity != null ? returnedQuantity : BigDecimal.ZERO;
        this.lastReturnedAt = lastReturnedAt;
        this.lastReturnedBy = lastReturnedBy;
        this.lastReturnReason = lastReturnReason;
    }

    public static WorkOrderPartAllocation record(TenantId tenantId, InventoryItemId itemId, UUID batchId, UUID workOrderId,
                                                 UUID taskId, BigDecimal quantity, Money unitCost) {
        return new WorkOrderPartAllocation(UUID.randomUUID(), tenantId, itemId, batchId, workOrderId, taskId, quantity,
                unitCost, Instant.now(), BigDecimal.ZERO, null, null, null);
    }

    /** Units still charged to the order. */
    public BigDecimal remainingQuantity() {
        return quantity.subtract(returnedQuantity).max(BigDecimal.ZERO);
    }

    public boolean isOpen() {
        return remainingQuantity().signum() > 0;
    }

    /**
     * Returns up to {@code requested} units to stock.
     *
     * @return units actually returned from this allocation
     */
    public BigDecimal returnUnits(BigDecimal requested, UUID returnedBy, String reason) {
        BigDecimal returned = requested.min(remainingQuantity());
        if (returned.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        this.returnedQuantity = this.returnedQuantity.add(returned);
        this.lastReturnedAt = Instant.now();
        this.lastReturnedBy = returnedBy;
        this.lastReturnReason = reason;
        return returned;
    }

    /** Acquisition cost of the units still charged to the order. */
    public BigDecimal costOfRemaining() {
        return unitCost.amount().multiply(remainingQuantity()).setScale(2, RoundingMode.HALF_EVEN);
    }

    public UUID getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public InventoryItemId getItemId() { return itemId; }
    public UUID getBatchId() { return batchId; }
    public UUID getWorkOrderId() { return workOrderId; }
    public UUID getTaskId() { return taskId; }
    public BigDecimal getQuantity() { return quantity; }
    public Money getUnitCost() { return unitCost; }
    public Instant getAllocatedAt() { return allocatedAt; }
    public BigDecimal getReturnedQuantity() { return returnedQuantity; }
    public Instant getLastReturnedAt() { return lastReturnedAt; }
    public UUID getLastReturnedBy() { return lastReturnedBy; }
    public String getLastReturnReason() { return lastReturnReason; }
}
