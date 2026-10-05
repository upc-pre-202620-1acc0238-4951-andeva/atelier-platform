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
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code purchase_order_items} relational table.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "purchase_order_items",
        indexes = {
                @Index(name = "idx_po_items_order", columnList = "purchase_order_id"),
                @Index(name = "idx_po_items_item", columnList = "item_id")
        }
)
public class PurchaseOrderItemPersistenceEntity extends InventoryChildPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrderPersistenceEntity purchaseOrder;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    public PurchaseOrderItemPersistenceEntity(UUID id) {
        super(id);
    }

    public PurchaseOrderItemPersistenceEntity(
            UUID id,
            PurchaseOrderPersistenceEntity purchaseOrder,
            UUID itemId,
            BigDecimal quantity,
            BigDecimal unitCost,
            BigDecimal totalCost
    ) {
        super(id);
        this.purchaseOrder = purchaseOrder;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
    }
}
