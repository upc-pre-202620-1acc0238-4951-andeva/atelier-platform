package com.andeva.atelier.platform.hr.domain.model.valueobjects;

import java.io.Serializable;

public record GracePeriod(int minutes) implements Serializable {

    public GracePeriod {
        if (minutes < 0 || minutes > 60) {
            throw new IllegalArgumentException("Grace period minutes must be between 0 and 60: " + minutes);
        }
    }

    public static GracePeriod of(int minutes) {
        return new GracePeriod(minutes);
    }

    public boolean hasExpired(long delayMinutes) {
        return delayMinutes > this.minutes;
    }
}
