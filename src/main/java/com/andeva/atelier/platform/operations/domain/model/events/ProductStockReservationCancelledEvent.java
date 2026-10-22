package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ProductStockReservationCancelledEvent(
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        UUID productId,
        Quantity quantity,
        Instant occurredOn,
        UUID removedBy
) implements Serializable {

    public ProductStockReservationCancelledEvent(WorkOrderId workOrderId, WorkOrderTaskId taskId, UUID productId,
                                                 Quantity quantity, Instant occurredOn) {
        this(workOrderId, taskId, productId, quantity, occurredOn, null);
    }

    /**
     * @param removedBy user who withdrew the part from the task (inventory keeps it for the audit trail)
     */
    public static ProductStockReservationCancelledEvent of(WorkOrderId workOrderId, WorkOrderTaskId taskId, UUID productId,
                                                          Quantity quantity, UUID removedBy) {
        return new ProductStockReservationCancelledEvent(workOrderId, taskId, productId, quantity, Instant.now(), removedBy);
    }
    public ProductStockReservationCancelledEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(taskId, "taskId cannot be null");
        Objects.requireNonNull(productId, "productId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static ProductStockReservationCancelledEvent of(WorkOrderId workOrderId, WorkOrderTaskId taskId, UUID productId, Quantity quantity) {
        return new ProductStockReservationCancelledEvent(workOrderId, taskId, productId, quantity, Instant.now());
    }
}
