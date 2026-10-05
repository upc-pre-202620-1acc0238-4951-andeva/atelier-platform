package com.andeva.atelier.platform.operations.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record WorkOrderId(UUID value) implements Serializable {

    public WorkOrderId {
        Objects.requireNonNull(value, "WorkOrderId value cannot be null");
    }

    public static WorkOrderId generate() {
        return new WorkOrderId(UUID.randomUUID());
    }

    public static WorkOrderId of(UUID value) {
        return new WorkOrderId(value);
    }
}
