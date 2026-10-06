package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.inventory.domain.model.enums.InventoryItemStatus;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.converters.InventoryItemStatusAttributeConverter;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.converters.ItemCategoryAttributeConverter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code inventory_items} relational table.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "inventory_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inventory_items_tenant_sku", columnNames = {"tenant_id", "sku"})
        },
        indexes = {
                @Index(name = "idx_inventory_items_tenant_sku", columnList = "tenant_id, sku"),
                @Index(name = "idx_inventory_items_category", columnList = "tenant_id, category")
        }
)
public class InventoryItemPersistenceEntity extends InventoryAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Convert(converter = ItemCategoryAttributeConverter.class)
    @Column(name = "category", nullable = false, length = 50)
    private ItemCategory category;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "total_stock", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalStock;

    @Column(name = "minimum_stock", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumStock;

    @Convert(converter = InventoryItemStatusAttributeConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private InventoryItemStatus status;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InventoryBatchPersistenceEntity> batches = new ArrayList<>();

    public InventoryItemPersistenceEntity(UUID id) {
        super(id);
    }

    public InventoryItemPersistenceEntity(
            UUID id,
            UUID tenantId,
            String name,
            String sku,
            ItemCategory category,
            BigDecimal basePrice,
            BigDecimal totalStock,
            BigDecimal minimumStock,
            InventoryItemStatus status
    ) {
        super(id);
        this.tenantId = tenantId;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.basePrice = basePrice;
        this.totalStock = totalStock;
        this.minimumStock = minimumStock;
        this.status = status;
    }

    public void addBatch(InventoryBatchPersistenceEntity batch) {
        this.batches.add(batch);
        batch.setItem(this);
    }

    public void removeBatch(InventoryBatchPersistenceEntity batch) {
        this.batches.remove(batch);
        batch.setItem(null);
    }
}
