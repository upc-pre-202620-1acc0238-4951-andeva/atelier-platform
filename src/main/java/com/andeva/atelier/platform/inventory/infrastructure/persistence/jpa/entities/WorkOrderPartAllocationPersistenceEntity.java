package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code work_order_part_allocations} table: one FIFO batch deduction
 * charged to a work order task, with the units returned since.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "work_order_part_allocations",
        indexes = {
                @Index(name = "idx_wo_part_alloc_order", columnList = "work_order_id, allocated_at"),
                @Index(name = "idx_wo_part_alloc_task_item", columnList = "work_order_id, task_id, item_id")
        }
)
public class WorkOrderPartAllocationPersistenceEntity extends InventoryAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "work_order_id", nullable = false)
    private UUID workOrderId;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "returned_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal returnedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "allocated_at", nullable = false)
    private Instant allocatedAt;

    @Column(name = "last_returned_at")
    private Instant lastReturnedAt;

    @Column(name = "last_returned_by")
    private UUID lastReturnedBy;

    @Column(name = "last_return_reason", length = 255)
    private String lastReturnReason;

    public WorkOrderPartAllocationPersistenceEntity(UUID id) {
        super(id);
    }
}
