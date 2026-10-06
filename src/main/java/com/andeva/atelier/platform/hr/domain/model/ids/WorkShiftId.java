package com.andeva.atelier.platform.hr.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record WorkShiftId(UUID value) implements Serializable {

    public WorkShiftId {
        Objects.requireNonNull(value, "WorkShiftId value cannot be null");
    }

    public static WorkShiftId generate() {
        return new WorkShiftId(UUID.randomUUID());
    }

    public static WorkShiftId of(UUID value) {
        return new WorkShiftId(value);
    }
}
