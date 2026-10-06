package com.andeva.atelier.platform.inventory.domain.model.aggregates;

import com.andeva.atelier.platform.inventory.domain.exceptions.InvalidPurchaseOrderTransitionException;
import com.andeva.atelier.platform.inventory.domain.exceptions.MissingReceiptDocumentationException;
import com.andeva.atelier.platform.inventory.domain.exceptions.PurchaseOrderEmptyException;
import com.andeva.atelier.platform.inventory.domain.model.entities.PurchaseOrderItem;
import com.andeva.atelier.platform.inventory.domain.model.enums.PurchaseOrderStatus;
import com.andeva.atelier.platform.inventory.domain.model.events.PurchaseOrderCanceledEvent;
import com.andeva.atelier.platform.inventory.domain.model.events.PurchaseOrderCreatedEvent;
import com.andeva.atelier.platform.inventory.domain.model.events.PurchaseOrderReceivedEvent;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.PurchaseOrderNumber;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PurchaseOrder extends AbstractDomainAggregateRoot<PurchaseOrder> {

    private final PurchaseOrderId id;
    private final TenantId tenantId;
    private final SupplierId supplierId;
    private final BranchId branchId;
    private final PurchaseOrderNumber orderNumber;
    private PurchaseOrderStatus status;
    private Money totalCost;
    private StorageUrl receiptImageUrl;
    private String receiptNumber;
    private Instant receivedAt;
    private final List<PurchaseOrderItem> items;

    public PurchaseOrder(
            PurchaseOrderId id,
            TenantId tenantId,
            SupplierId supplierId,
            BranchId branchId,
            PurchaseOrderNumber orderNumber,
            PurchaseOrderStatus status,
            Money totalCost,
            StorageUrl receiptImageUrl,
            String receiptNumber,
            Instant receivedAt,
            List<PurchaseOrderItem> items
    ) {
        this.id = Objects.requireNonNull(id, "PurchaseOrderId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.supplierId = Objects.requireNonNull(supplierId, "SupplierId cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "BranchId cannot be null");
        this.orderNumber = Objects.requireNonNull(orderNumber, "orderNumber cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.totalCost = totalCost != null ? totalCost : Money.ZERO_PEN;
        this.receiptImageUrl = receiptImageUrl;
        this.receiptNumber = receiptNumber;
        this.receivedAt = receivedAt;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public static PurchaseOrder create(
            TenantId tenantId,
            SupplierId supplierId,
            BranchId branchId,
            PurchaseOrderNumber orderNumber
    ) {
        PurchaseOrder order = new PurchaseOrder(
                PurchaseOrderId.generate(),
                tenantId,
                supplierId,
                branchId,
                orderNumber,
                PurchaseOrderStatus.DRAFT,
                Money.ZERO_PEN,
                null,
                null,
                null,
                new ArrayList<>()
        );
        order.registerDomainEvent(PurchaseOrderCreatedEvent.of(order.id, tenantId, supplierId));
        return order;
    }

    public static PurchaseOrder reconstitute(
            PurchaseOrderId id,
            TenantId tenantId,
            SupplierId supplierId,
            BranchId branchId,
            PurchaseOrderNumber orderNumber,
            PurchaseOrderStatus status,
            Money totalCost,
            StorageUrl receiptImageUrl,
            String receiptNumber,
            Instant receivedAt,
            List<PurchaseOrderItem> items
    ) {
        return new PurchaseOrder(
                id,
                tenantId,
                supplierId,
                branchId,
                orderNumber,
                status,
                totalCost,
                receiptImageUrl,
                receiptNumber,
                receivedAt,
                items
        );
    }

    public void addItem(PurchaseOrderItem item) {
        Objects.requireNonNull(item, "item cannot be null");
        if (!this.status.canBeModified()) {
            throw new InvalidPurchaseOrderTransitionException("Cannot add items to purchase order in status: " + this.status);
        }
        this.items.add(item);
        recalculateTotalCost();
    }

    public void removeItem(PurchaseOrderItemId itemId) {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        if (!this.status.canBeModified()) {
            throw new InvalidPurchaseOrderTransitionException("Cannot remove items from purchase order in status: " + this.status);
        }
        this.items.removeIf(item -> item.getId().equals(itemId));
        recalculateTotalCost();
    }

    public void issue() {
        if (!this.status.canBeIssued()) {
            throw new InvalidPurchaseOrderTransitionException("Cannot issue purchase order in status: " + this.status);
        }
        if (this.items.isEmpty()) {
            throw new PurchaseOrderEmptyException("Cannot issue an empty purchase order");
        }
        this.status = PurchaseOrderStatus.ISSUED;
    }

    public void receive(StorageUrl receiptImageUrl, String receiptNumber, Instant receivedAt) {
        if (!this.status.canBeReceived()) {
            throw new InvalidPurchaseOrderTransitionException("Cannot receive purchase order in status: " + this.status + ". Must be in ISSUED status.");
        }
        if (receiptImageUrl == null || receiptNumber == null || receiptNumber.isBlank()) {
            throw new MissingReceiptDocumentationException("Receipt image URL and receipt number are mandatory to receive purchase order");
        }
        this.receiptImageUrl = receiptImageUrl;
        this.receiptNumber = receiptNumber.trim();
        this.receivedAt = receivedAt != null ? receivedAt : Instant.now();
        this.status = PurchaseOrderStatus.RECEIVED;

        registerDomainEvent(PurchaseOrderReceivedEvent.of(this.id, this.tenantId, this.supplierId, this.totalCost));
    }

    public void cancel(String reason) {
        if (!this.status.canBeCanceled()) {
            throw new InvalidPurchaseOrderTransitionException("Cannot cancel purchase order in terminal status: " + this.status);
        }
        this.status = PurchaseOrderStatus.CANCELED;
        registerDomainEvent(PurchaseOrderCanceledEvent.of(this.id, this.tenantId, reason != null ? reason : "Canceled by user"));
    }

    public void addItem(InventoryItemId itemId, Quantity quantity, Money unitCost) {
        addItem(PurchaseOrderItem.create(this.id, itemId, quantity, unitCost));
    }

    public void issueOrder() {
        issue();
    }

    public void receiveOrder(StorageUrl receiptImageUrl, String receiptNumber, Instant receivedAt) {
        receive(receiptImageUrl, receiptNumber, receivedAt);
    }

    public void cancelOrder(String reason) {
        cancel(reason);
    }

    private void recalculateTotalCost() {
        BigDecimal sum = BigDecimal.ZERO;
        Currency currency = Currency.PEN;
        for (PurchaseOrderItem item : items) {
            sum = sum.add(item.getTotalCost().amount());
            currency = item.getTotalCost().currency();
        }
        this.totalCost = Money.of(sum, currency);
    }

    public PurchaseOrderId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public SupplierId getSupplierId() { return supplierId; }
    public BranchId getBranchId() { return branchId; }
    public PurchaseOrderNumber getOrderNumber() { return orderNumber; }
    public PurchaseOrderStatus getStatus() { return status; }
    public Money getTotalCost() { return totalCost; }
    public Optional<StorageUrl> getReceiptImageUrl() { return Optional.ofNullable(receiptImageUrl); }
    public Optional<String> getReceiptNumber() { return Optional.ofNullable(receiptNumber); }
    public Optional<Instant> getReceivedAt() { return Optional.ofNullable(receivedAt); }
    public List<PurchaseOrderItem> getItems() { return Collections.unmodifiableList(items); }

    public void addExistingItem(PurchaseOrderItem item) {
        if (item != null) {
            this.items.add(item);
        }
    }
}
