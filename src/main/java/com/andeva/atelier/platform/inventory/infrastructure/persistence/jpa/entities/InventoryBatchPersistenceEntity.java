package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code inventory_batches} relational table.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "inventory_batches",
        indexes = {
                @Index(name = "idx_inventory_batches_item_fifo", columnList = "item_id, arrival_date"),
                @Index(name = "idx_inventory_batches_tenant", columnList = "tenant_id"),
                @Index(name = "idx_inventory_batches_po", columnList = "purchase_order_id"),
                @Index(name = "idx_inventory_batches_supplier", columnList = "supplier_id")
        }
)
public class InventoryBatchPersistenceEntity extends InventoryChildPersistenceEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItemPersistenceEntity item;

    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "purchase_order_id")
    private UUID purchaseOrderId;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "receipt_image_url", length = 255)
    private String receiptImageUrl;

    @Column(name = "initial_qty", nullable = false, precision = 10, scale = 2)
    private BigDecimal initialQty;

    @Column(name = "remaining_qty", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingQty;

    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "arrival_date", nullable = false)
    private Instant arrivalDate;

    public InventoryBatchPersistenceEntity(UUID id) {
        super(id);
    }

    public InventoryBatchPersistenceEntity(
            UUID id,
            UUID tenantId,
            InventoryItemPersistenceEntity item,
            UUID supplierId,
            UUID purchaseOrderId,
            String batchNumber,
            String receiptImageUrl,
            BigDecimal initialQty,
            BigDecimal remainingQty,
            BigDecimal unitCost,
            Instant arrivalDate
    ) {
        super(id);
        this.tenantId = tenantId;
        this.item = item;
        this.supplierId = supplierId;
        this.purchaseOrderId = purchaseOrderId;
        this.batchNumber = batchNumber;
        this.receiptImageUrl = receiptImageUrl;
        this.initialQty = initialQty;
        this.remainingQty = remainingQty;
        this.unitCost = unitCost;
        this.arrivalDate = arrivalDate;
    }
}
