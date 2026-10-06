package com.andeva.atelier.platform.hr.domain.model.valueobjects;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

public record ShiftSchedule(LocalTime startTime, LocalTime endTime, boolean spansOverMidnight) implements Serializable {

    public ShiftSchedule {
        Objects.requireNonNull(startTime, "startTime cannot be null");
        Objects.requireNonNull(endTime, "endTime cannot be null");
        if (startTime.equals(endTime)) {
            throw new IllegalArgumentException("startTime and endTime cannot be identical: " + startTime);
        }
    }

    public static ShiftSchedule of(LocalTime startTime, LocalTime endTime) {
        boolean spansMidnight = endTime.isBefore(startTime);
        return new ShiftSchedule(startTime, endTime, spansMidnight);
    }

    public long durationMinutes() {
        if (spansOverMidnight) {
            long untilMidnight = Duration.between(startTime, LocalTime.MAX).toMinutes() + 1;
            long fromMidnight = Duration.between(LocalTime.MIN, endTime).toMinutes();
            return untilMidnight + fromMidnight;
        } else {
            return Duration.between(startTime, endTime).toMinutes();
        }
    }

    public boolean isWithinWindow(LocalTime time) {
        if (time == null) return false;
        if (spansOverMidnight) {
            return !time.isBefore(startTime) || !time.isAfter(endTime);
        } else {
            return !time.isBefore(startTime) && !time.isAfter(endTime);
        }
    }
}
