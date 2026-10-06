package com.andeva.atelier.platform.hr.domain.model.valueobjects;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record PayPeriod(LocalDate startDate, LocalDate endDate) implements Serializable {

    public PayPeriod {
        Objects.requireNonNull(startDate, "startDate cannot be null");
        Objects.requireNonNull(endDate, "endDate cannot be null");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate cannot be before startDate: " + startDate + " - " + endDate);
        }
    }

    public static PayPeriod of(LocalDate startDate, LocalDate endDate) {
        return new PayPeriod(startDate, endDate);
    }

    public long totalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public boolean contains(LocalDate date) {
        if (date == null) return false;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
