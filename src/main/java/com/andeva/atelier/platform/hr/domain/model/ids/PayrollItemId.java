package com.andeva.atelier.platform.hr.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record PayrollItemId(UUID value) implements Serializable {

    public PayrollItemId {
        Objects.requireNonNull(value, "PayrollItemId value cannot be null");
    }

    public static PayrollItemId generate() {
        return new PayrollItemId(UUID.randomUUID());
    }

    public static PayrollItemId of(UUID value) {
        return new PayrollItemId(value);
    }
}
