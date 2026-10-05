package com.andeva.atelier.platform.operations.domain.model.valueobjects;

import java.io.Serializable;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Pattern;

public record WorkOrderNumber(String value) implements Serializable {

    private static final Pattern PATTERN = Pattern.compile("^WO-[0-9]{6}-[0-9]{4}$");
    private static final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    public WorkOrderNumber {
        Objects.requireNonNull(value, "WorkOrderNumber value cannot be null");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("WorkOrderNumber must follow format WO-YYYYMM-XXXX: " + value);
        }
    }

    public static WorkOrderNumber of(String value) {
        return new WorkOrderNumber(value);
    }

    public static WorkOrderNumber of(int sequence) {
        String ym = YearMonth.now().format(YYYYMM);
        return new WorkOrderNumber(String.format("WO-%s-%04d", ym, sequence));
    }

    public int sequence() {
        String[] parts = value.split("-");
        return Integer.parseInt(parts[2]);
    }
}
