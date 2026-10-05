package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ProductStockReservationRequestedEvent(
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        UUID productId,
        Quantity quantity,
        Instant occurredOn
) implements Serializable {
    public ProductStockReservationRequestedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(taskId, "taskId cannot be null");
        Objects.requireNonNull(productId, "productId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static ProductStockReservationRequestedEvent of(WorkOrderId workOrderId, WorkOrderTaskId taskId, UUID productId, Quantity quantity) {
        return new ProductStockReservationRequestedEvent(workOrderId, taskId, productId, quantity, Instant.now());
    }
}
