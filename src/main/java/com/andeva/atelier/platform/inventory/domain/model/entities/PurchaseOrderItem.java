package com.andeva.atelier.platform.inventory.domain.model.entities;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class PurchaseOrderItem implements Serializable {

    private final PurchaseOrderItemId id;
    private final PurchaseOrderId orderId;
    private final InventoryItemId itemId;
    private Quantity quantity;
    private final Money unitCost;
    private Money totalCost;

    public PurchaseOrderItem(
            PurchaseOrderItemId id,
            PurchaseOrderId orderId,
            InventoryItemId itemId,
            Quantity quantity,
            Money unitCost,
            Money totalCost
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "orderId cannot be null");
        this.itemId = Objects.requireNonNull(itemId, "itemId cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "quantity cannot be null");
        this.unitCost = Objects.requireNonNull(unitCost, "unitCost cannot be null");
        this.totalCost = totalCost != null ? totalCost : calculateTotalCost(quantity, unitCost);

        if (this.quantity.isZero()) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    public static PurchaseOrderItem create(
            PurchaseOrderId orderId,
            InventoryItemId itemId,
            Quantity quantity,
            Money unitCost
    ) {
        Money total = calculateTotalCost(quantity, unitCost);
        return new PurchaseOrderItem(PurchaseOrderItemId.generate(), orderId, itemId, quantity, unitCost, total);
    }

    public static PurchaseOrderItem reconstitute(
            PurchaseOrderItemId id,
            PurchaseOrderId orderId,
            InventoryItemId itemId,
            Quantity quantity,
            Money unitCost,
            Money totalCost
    ) {
        return new PurchaseOrderItem(id, orderId, itemId, quantity, unitCost, totalCost);
    }

    private static Money calculateTotalCost(Quantity quantity, Money unitCost) {
        BigDecimal total = unitCost.amount().multiply(quantity.value()).setScale(2, RoundingMode.HALF_EVEN);
        return Money.of(total, unitCost.currency());
    }

    public void updateQuantity(Quantity newQuantity) {
        Objects.requireNonNull(newQuantity, "newQuantity cannot be null");
        if (newQuantity.isZero()) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = newQuantity;
        this.totalCost = calculateTotalCost(this.quantity, this.unitCost);
    }

    public PurchaseOrderItemId getId() { return id; }
    public PurchaseOrderId getOrderId() { return orderId; }
    public InventoryItemId getItemId() { return itemId; }
    public Quantity getQuantity() { return quantity; }
    public Money getUnitCost() { return unitCost; }
    public Money getTotalCost() { return totalCost; }
}
