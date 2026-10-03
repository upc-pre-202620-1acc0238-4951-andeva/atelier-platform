package com.andeva.atelier.platform.billing.domain.model.valueobjects;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable value object defining the start and end boundaries of a subscription cycle.
 *
 * @author Joel Huamani Estefanero
 */
public record SubscriptionPeriod(
        Instant startDate,
        Instant endDate
) implements Serializable {

    public SubscriptionPeriod {
        Objects.requireNonNull(startDate, "Subscription period start date cannot be null");
        Objects.requireNonNull(endDate, "Subscription period end date cannot be null");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Subscription period start date cannot be after end date");
        }
    }

    public static SubscriptionPeriod of(Instant startDate, Instant endDate) {
        return new SubscriptionPeriod(startDate, endDate);
    }

    /**
     * Evaluates if the given timestamp falls inside this period (inclusive of boundaries).
     *
     * @param timestamp timestamp to test
     * @return true if timestamp is within [startDate, endDate]
     */
    public boolean isActiveAt(Instant timestamp) {
        if (timestamp == null) {
            return false;
        }
        return !timestamp.isBefore(startDate) && !timestamp.isAfter(endDate);
    }

    /**
     * Calculates the duration between startDate and endDate.
     *
     * @return the Duration between startDate and endDate
     */
    public Duration duration() {
        return Duration.between(startDate, endDate);
    }
}
