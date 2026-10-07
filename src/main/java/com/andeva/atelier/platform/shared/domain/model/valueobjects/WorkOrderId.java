package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a Workshop Operations Work Order.
 *
 * @author Joel Huamani Estefanero
 */
public record WorkOrderId(UUID value) implements Serializable {

    public WorkOrderId {
        Objects.requireNonNull(value, "Work order identifier cannot be null");
    }

    public static WorkOrderId of(UUID value) {
        return new WorkOrderId(value);
    }

    public static WorkOrderId of(String value) {
        Objects.requireNonNull(value, "Work order identifier cannot be null");
        return new WorkOrderId(UUID.fromString(value));
    }

    public static WorkOrderId generate() {
        return new WorkOrderId(UUID.randomUUID());
    }
}
