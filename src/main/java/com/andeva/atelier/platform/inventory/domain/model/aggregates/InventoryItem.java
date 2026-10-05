package com.andeva.atelier.platform.inventory.domain.model.aggregates;

import com.andeva.atelier.platform.inventory.domain.exceptions.InsufficientStockException;
import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.enums.InventoryItemStatus;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.events.*;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.*;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.util.*;

public class InventoryItem extends AbstractDomainAggregateRoot<InventoryItem> {

    private final InventoryItemId id;
    private final TenantId tenantId;
    private String name;
    private final Sku sku;
    private ItemCategory category;
    private Money basePrice;
    private Quantity totalStock;
    private Quantity minimumStock;
    private String unitOfMeasure;
    private InventoryItemStatus status;
    private final List<InventoryBatch> batches;

    public InventoryItem(
            InventoryItemId id,
            TenantId tenantId,
            String name,
            Sku sku,
            ItemCategory category,
            Money basePrice,
            Quantity totalStock,
            Quantity minimumStock,
            String unitOfMeasure,
            InventoryItemStatus status,
            List<InventoryBatch> batches
    ) {
        this.id = Objects.requireNonNull(id, "InventoryItemId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.sku = Objects.requireNonNull(sku, "Sku cannot be null");
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.basePrice = Objects.requireNonNull(basePrice, "BasePrice cannot be null");
        this.totalStock = totalStock != null ? totalStock : Quantity.ZERO;
        this.minimumStock = minimumStock != null ? minimumStock : Quantity.ZERO;
        this.unitOfMeasure = unitOfMeasure != null ? unitOfMeasure : "unit";
        this.status = status != null ? status : InventoryItemStatus.ACTIVE;
        this.batches = batches != null ? new ArrayList<>(batches) : new ArrayList<>();
    }

    public static InventoryItem create(
            TenantId tenantId,
            String name,
            Sku sku,
            ItemCategory category,
            Money basePrice,
            Quantity minimumStock,
            String unitOfMeasure
    ) {
        InventoryItem item = new InventoryItem(
                InventoryItemId.generate(),
                tenantId,
                name,
                sku,
                category,
                basePrice,
                Quantity.ZERO,
                minimumStock,
                unitOfMeasure,
                InventoryItemStatus.ACTIVE,
                new ArrayList<>()
        );
        item.registerDomainEvent(InventoryItemCreatedEvent.of(item.id, tenantId, sku, name));
        return item;
    }

    public static InventoryItem reconstitute(
            InventoryItemId id,
            TenantId tenantId,
            String name,
            Sku sku,
            ItemCategory category,
            Money basePrice,
            Quantity totalStock,
            Quantity minimumStock,
            String unitOfMeasure,
            InventoryItemStatus status,
            List<InventoryBatch> batches
    ) {
        return new InventoryItem(
                id,
                tenantId,
                name,
                sku,
                category,
                basePrice,
                totalStock,
                minimumStock,
                unitOfMeasure,
                status,
                batches
        );
    }

    public void addBatch(InventoryBatch batch) {
        Objects.requireNonNull(batch, "batch cannot be null");
        this.batches.add(batch);
        // Ensure chronological FIFO ordering by arrivalDate
        this.batches.sort(Comparator.comparing(InventoryBatch::getArrivalDate));
        this.totalStock = this.totalStock.add(batch.getInitialQuantity());
        registerDomainEvent(InventoryBatchAddedEvent.of(batch.getId(), this.id, batch.getInitialQuantity(), batch.getUnitCost()));
    }

    public StockAllocation allocateStockFifo(Quantity requestedQuantity) {
        Objects.requireNonNull(requestedQuantity, "requestedQuantity cannot be null");
        if (requestedQuantity.isZero()) {
            throw new IllegalArgumentException("Requested quantity must be greater than zero");
        }
        if (this.totalStock.isLessThan(requestedQuantity)) {
            throw new InsufficientStockException("Insufficient stock for item SKU " + this.sku.value()
                    + ". Requested: " + requestedQuantity.value() + ", Available: " + this.totalStock.value());
        }

        // Sort by arrivalDate ASC for strict FIFO
        this.batches.sort(Comparator.comparing(InventoryBatch::getArrivalDate));

        Quantity remainingToDeduct = requestedQuantity;
        List<BatchDeduction> deductions = new ArrayList<>();
        BigDecimal totalCogsAmount = BigDecimal.ZERO;
        Currency currency = this.basePrice.currency();

        for (InventoryBatch batch : this.batches) {
            if (!batch.hasStock()) {
                continue;
            }

            Quantity deductedFromBatch = batch.deduct(remainingToDeduct);
            if (deductedFromBatch.isPositive()) {
                BatchDeduction deduction = new BatchDeduction(batch.getId().value(), deductedFromBatch, batch.getUnitCost());
                deductions.add(deduction);
                totalCogsAmount = totalCogsAmount.add(deduction.subtotal().amount());
                currency = batch.getUnitCost().currency();

                remainingToDeduct = remainingToDeduct.subtract(deductedFromBatch);
                if (remainingToDeduct.isZero()) {
                    break;
                }
            }
        }

        this.totalStock = this.totalStock.subtract(requestedQuantity);
        Money totalCogs = Money.of(totalCogsAmount, currency);
        StockAllocation allocation = StockAllocation.of(requestedQuantity, totalCogs, deductions);

        registerDomainEvent(StockAllocatedFifoEvent.of(this.id, requestedQuantity, totalCogs));

        if (this.totalStock.isLessThanOrEqualTo(this.minimumStock)) {
            registerDomainEvent(LowStockThresholdReachedEvent.of(this.id, this.tenantId, this.totalStock, this.minimumStock));
        }

        return allocation;
    }

    public void releaseStockAllocation(StockAllocation allocation) {
        Objects.requireNonNull(allocation, "allocation cannot be null");
        for (BatchDeduction deduction : allocation.deductions()) {
            for (InventoryBatch batch : this.batches) {
                if (batch.getId().value().equals(deduction.batchId())) {
                    batch.restore(deduction.quantityDeducted());
                    break;
                }
            }
        }
        this.totalStock = this.totalStock.add(allocation.allocatedQuantity());
        registerDomainEvent(StockReleasedEvent.of(this.id, allocation.allocatedQuantity()));
    }

    public void restoreBatchStock(UUID batchId, Quantity quantityToRestore) {
        Objects.requireNonNull(batchId, "batchId cannot be null");
        Objects.requireNonNull(quantityToRestore, "quantityToRestore cannot be null");
        for (InventoryBatch batch : this.batches) {
            if (batch.getId().value().equals(batchId)) {
                batch.restore(quantityToRestore);
                this.totalStock = this.totalStock.add(quantityToRestore);
                registerDomainEvent(StockReleasedEvent.of(this.id, quantityToRestore));
                return;
            }
        }
    }

    public void updateDetails(String name, ItemCategory category, Money basePrice, Quantity minimumStock, String unitOfMeasure) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (category != null) {
            this.category = category;
        }
        if (basePrice != null) {
            this.basePrice = basePrice;
        }
        if (minimumStock != null) {
            this.minimumStock = minimumStock;
        }
        if (unitOfMeasure != null && !unitOfMeasure.isBlank()) {
            this.unitOfMeasure = unitOfMeasure;
        }
    }

    public void deactivate() {
        this.status = InventoryItemStatus.INACTIVE;
        registerDomainEvent(InventoryItemDeactivatedEvent.of(this.id, this.tenantId));
    }

    public void activate() {
        this.status = InventoryItemStatus.ACTIVE;
    }

    public void discontinue() {
        this.status = InventoryItemStatus.DISCONTINUED;
    }

    public InventoryItemId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public String getName() { return name; }
    public Sku getSku() { return sku; }
    public ItemCategory getCategory() { return category; }
    public Money getBasePrice() { return basePrice; }
    public Quantity getTotalStock() { return totalStock; }
    public Quantity getMinimumStock() { return minimumStock; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public InventoryItemStatus getStatus() { return status; }
    public List<InventoryBatch> getBatches() { return Collections.unmodifiableList(batches); }

    public void addExistingBatch(InventoryBatch batch) {
        if (batch != null) {
            this.batches.add(batch);
        }
    }
}
