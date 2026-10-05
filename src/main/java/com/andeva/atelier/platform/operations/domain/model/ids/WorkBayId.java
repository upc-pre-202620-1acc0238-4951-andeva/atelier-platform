package com.andeva.atelier.platform.operations.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record WorkBayId(UUID value) implements Serializable {

    public WorkBayId {
        Objects.requireNonNull(value, "WorkBayId value cannot be null");
    }

    public static WorkBayId generate() {
        return new WorkBayId(UUID.randomUUID());
    }

    public static WorkBayId of(UUID value) {
        return new WorkBayId(value);
    }
}
