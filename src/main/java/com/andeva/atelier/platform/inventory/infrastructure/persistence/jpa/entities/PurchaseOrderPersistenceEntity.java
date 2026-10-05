package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.inventory.domain.model.enums.PurchaseOrderStatus;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.converters.PurchaseOrderStatusAttributeConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code purchase_orders} relational table.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "purchase_orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_purchase_orders_tenant_number", columnNames = {"tenant_id", "order_number"})
        },
        indexes = {
                @Index(name = "idx_purchase_orders_tenant_status", columnList = "tenant_id, status"),
                @Index(name = "idx_purchase_orders_supplier", columnList = "supplier_id"),
                @Index(name = "idx_purchase_orders_branch", columnList = "branch_id")
        }
)
public class PurchaseOrderPersistenceEntity extends InventoryAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Convert(converter = PurchaseOrderStatusAttributeConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private PurchaseOrderStatus status;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "receipt_image_url", length = 255)
    private String receiptImageUrl;

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(name = "received_at")
    private Instant receivedAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PurchaseOrderItemPersistenceEntity> items = new ArrayList<>();

    public PurchaseOrderPersistenceEntity(UUID id) {
        super(id);
    }

    public PurchaseOrderPersistenceEntity(
            UUID id,
            UUID tenantId,
            UUID supplierId,
            UUID branchId,
            String orderNumber,
            PurchaseOrderStatus status,
            BigDecimal totalCost,
            String receiptImageUrl,
            String receiptNumber,
            Instant receivedAt
    ) {
        super(id);
        this.tenantId = tenantId;
        this.supplierId = supplierId;
        this.branchId = branchId;
        this.orderNumber = orderNumber;
        this.status = status;
        this.totalCost = totalCost;
        this.receiptImageUrl = receiptImageUrl;
        this.receiptNumber = receiptNumber;
        this.receivedAt = receivedAt;
    }

    public void addItem(PurchaseOrderItemPersistenceEntity item) {
        this.items.add(item);
        item.setPurchaseOrder(this);
    }

    public void removeItem(PurchaseOrderItemPersistenceEntity item) {
        this.items.remove(item);
        item.setPurchaseOrder(null);
    }
}
