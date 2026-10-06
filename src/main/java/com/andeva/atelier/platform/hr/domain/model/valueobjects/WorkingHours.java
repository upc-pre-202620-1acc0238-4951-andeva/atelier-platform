package com.andeva.atelier.platform.hr.domain.model.valueobjects;

import java.io.Serializable;

public record WorkingHours(double hours) implements Serializable {

    public WorkingHours {
        if (hours < 0.0) {
            throw new IllegalArgumentException("Working hours cannot be negative: " + hours);
        }
    }

    public static WorkingHours of(double hours) {
        return new WorkingHours(hours);
    }

    public static WorkingHours fromMinutes(long minutes) {
        return new WorkingHours(minutes / 60.0);
    }
}
