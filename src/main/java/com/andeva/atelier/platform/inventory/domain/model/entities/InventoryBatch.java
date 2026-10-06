package com.andeva.atelier.platform.inventory.domain.model.entities;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryBatchId;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class InventoryBatch implements Serializable {

    private final InventoryBatchId id;
    private final TenantId tenantId;
    private final InventoryItemId itemId;
    private final SupplierId supplierId;
    private final PurchaseOrderId purchaseOrderId;
    private final String batchNumber;
    private final Quantity initialQuantity;
    private Quantity remainingQuantity;
    private final Money unitCost;
    private final Instant arrivalDate;
    private StorageUrl receiptImageUrl;

    public InventoryBatch(
            InventoryBatchId id,
            TenantId tenantId,
            InventoryItemId itemId,
            SupplierId supplierId,
            PurchaseOrderId purchaseOrderId,
            String batchNumber,
            Quantity initialQuantity,
            Quantity remainingQuantity,
            Money unitCost,
            Instant arrivalDate,
            StorageUrl receiptImageUrl
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId cannot be null");
        this.itemId = Objects.requireNonNull(itemId, "itemId cannot be null");
        this.supplierId = supplierId;
        this.purchaseOrderId = purchaseOrderId;
        this.batchNumber = Objects.requireNonNull(batchNumber, "batchNumber cannot be null");
        this.initialQuantity = Objects.requireNonNull(initialQuantity, "initialQuantity cannot be null");
        this.remainingQuantity = Objects.requireNonNull(remainingQuantity, "remainingQuantity cannot be null");
        this.unitCost = Objects.requireNonNull(unitCost, "unitCost cannot be null");
        this.arrivalDate = Objects.requireNonNull(arrivalDate, "arrivalDate cannot be null");
        this.receiptImageUrl = receiptImageUrl;

        if (this.initialQuantity.isZero()) {
            throw new IllegalArgumentException("initialQuantity must be greater than zero");
        }
        if (this.remainingQuantity.isGreaterThan(this.initialQuantity)) {
            throw new IllegalArgumentException("remainingQuantity cannot exceed initialQuantity");
        }
    }

    public static InventoryBatch create(
            TenantId tenantId,
            InventoryItemId itemId,
            SupplierId supplierId,
            PurchaseOrderId purchaseOrderId,
            String batchNumber,
            Quantity initialQuantity,
            Money unitCost,
            Instant arrivalDate,
            StorageUrl receiptImageUrl
    ) {
        return new InventoryBatch(
                InventoryBatchId.generate(),
                tenantId,
                itemId,
                supplierId,
                purchaseOrderId,
                batchNumber,
                initialQuantity,
                initialQuantity,
                unitCost,
                arrivalDate != null ? arrivalDate : Instant.now(),
                receiptImageUrl
        );
    }

    public static InventoryBatch reconstitute(
            InventoryBatchId id,
            TenantId tenantId,
            InventoryItemId itemId,
            SupplierId supplierId,
            PurchaseOrderId purchaseOrderId,
            String batchNumber,
            Quantity initialQuantity,
            Quantity remainingQuantity,
            Money unitCost,
            Instant arrivalDate,
            StorageUrl receiptImageUrl
    ) {
        return new InventoryBatch(
                id,
                tenantId,
                itemId,
                supplierId,
                purchaseOrderId,
                batchNumber,
                initialQuantity,
                remainingQuantity,
                unitCost,
                arrivalDate,
                receiptImageUrl
        );
    }

    public boolean hasStock() {
        return remainingQuantity.isPositive();
    }

    public boolean isDepleted() {
        return !hasStock();
    }

    public Quantity deduct(Quantity requested) {
        Objects.requireNonNull(requested, "requested quantity cannot be null");
        if (!hasStock() || requested.isZero()) {
            return Quantity.ZERO;
        }

        if (remainingQuantity.isLessThanOrEqualTo(requested)) {
            Quantity deducted = this.remainingQuantity;
            this.remainingQuantity = Quantity.ZERO;
            return deducted;
        } else {
            this.remainingQuantity = this.remainingQuantity.subtract(requested);
            return requested;
        }
    }

    public void restore(Quantity quantityToRestore) {
        Objects.requireNonNull(quantityToRestore, "quantityToRestore cannot be null");
        Quantity newRemaining = this.remainingQuantity.add(quantityToRestore);
        if (newRemaining.isGreaterThan(this.initialQuantity)) {
            this.remainingQuantity = this.initialQuantity;
        } else {
            this.remainingQuantity = newRemaining;
        }
    }

    public void setReceiptImageUrl(StorageUrl receiptImageUrl) {
        this.receiptImageUrl = receiptImageUrl;
    }

    public InventoryBatchId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public InventoryItemId getItemId() { return itemId; }
    public Optional<SupplierId> getSupplierId() { return Optional.ofNullable(supplierId); }
    public Optional<PurchaseOrderId> getPurchaseOrderId() { return Optional.ofNullable(purchaseOrderId); }
    public String getBatchNumber() { return batchNumber; }
    public Quantity getInitialQuantity() { return initialQuantity; }
    public Quantity getRemainingQuantity() { return remainingQuantity; }
    public Money getUnitCost() { return unitCost; }
    public Instant getArrivalDate() { return arrivalDate; }
    public Optional<StorageUrl> getReceiptImageUrl() { return Optional.ofNullable(receiptImageUrl); }
}
