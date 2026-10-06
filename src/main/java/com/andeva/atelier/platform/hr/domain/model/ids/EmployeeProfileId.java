package com.andeva.atelier.platform.hr.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record EmployeeProfileId(UUID value) implements Serializable {

    public EmployeeProfileId {
        Objects.requireNonNull(value, "EmployeeProfileId value cannot be null");
    }

    public static EmployeeProfileId generate() {
        return new EmployeeProfileId(UUID.randomUUID());
    }

    public static EmployeeProfileId of(UUID value) {
        return new EmployeeProfileId(value);
    }
}
