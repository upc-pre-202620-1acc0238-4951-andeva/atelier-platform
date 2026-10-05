package com.andeva.atelier.platform.operations.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record WorkOrderTaskProductId(UUID value) implements Serializable {

    public WorkOrderTaskProductId {
        Objects.requireNonNull(value, "WorkOrderTaskProductId value cannot be null");
    }

    public static WorkOrderTaskProductId generate() {
        return new WorkOrderTaskProductId(UUID.randomUUID());
    }

    public static WorkOrderTaskProductId of(UUID value) {
        return new WorkOrderTaskProductId(value);
    }
}
