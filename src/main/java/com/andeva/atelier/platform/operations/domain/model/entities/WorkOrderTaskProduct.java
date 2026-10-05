package com.andeva.atelier.platform.operations.domain.model.entities;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskProductId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.util.Objects;
import java.util.UUID;

public class WorkOrderTaskProduct {
    private final WorkOrderTaskProductId id;
    private final WorkOrderTaskId taskId;
    private final UUID productId;
    private Quantity quantity;
    private Money unitPrice;
    private Money totalAmount;

    public WorkOrderTaskProduct(WorkOrderTaskProductId id, WorkOrderTaskId taskId, UUID productId, Quantity quantity, Money unitPrice, Money totalAmount) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.taskId = Objects.requireNonNull(taskId, "taskId cannot be null");
        this.productId = Objects.requireNonNull(productId, "productId cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "quantity cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice cannot be null");
        this.totalAmount = totalAmount != null ? totalAmount : unitPrice.multiply(quantity.value());
    }

    public static WorkOrderTaskProduct create(WorkOrderTaskId taskId, UUID productId, Quantity quantity, Money unitPrice) {
        return new WorkOrderTaskProduct(
                WorkOrderTaskProductId.generate(),
                taskId,
                productId,
                quantity,
                unitPrice,
                unitPrice.multiply(quantity.value())
        );
    }

    public void updateQuantity(Quantity newQuantity) {
        this.quantity = Objects.requireNonNull(newQuantity, "newQuantity cannot be null");
        this.totalAmount = this.unitPrice.multiply(this.quantity.value());
    }

    public WorkOrderTaskProductId getId() { return id; }
    public WorkOrderTaskId getTaskId() { return taskId; }
    public UUID getProductId() { return productId; }
    public Quantity getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
    public Money getTotalAmount() { return totalAmount; }
}
