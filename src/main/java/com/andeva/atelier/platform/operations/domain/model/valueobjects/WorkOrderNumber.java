package com.andeva.atelier.platform.operations.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public record WorkOrderNumber(String value) implements Serializable {

    private static final Pattern PATTERN = Pattern.compile("^WO-[0-9]{6}-[0-9]{4}$");

    public WorkOrderNumber {
        Objects.requireNonNull(value, "WorkOrderNumber value cannot be null");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("WorkOrderNumber must follow format WO-YYYYMM-XXXX: " + value);
        }
    }

    public static WorkOrderNumber of(String value) {
        return new WorkOrderNumber(value);
    }
}
