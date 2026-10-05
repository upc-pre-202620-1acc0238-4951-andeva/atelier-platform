package com.andeva.atelier.platform.operations.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record WorkOrderTaskId(UUID value) implements Serializable {

    public WorkOrderTaskId {
        Objects.requireNonNull(value, "WorkOrderTaskId value cannot be null");
    }

    public static WorkOrderTaskId generate() {
        return new WorkOrderTaskId(UUID.randomUUID());
    }

    public static WorkOrderTaskId of(UUID value) {
        return new WorkOrderTaskId(value);
    }
}
