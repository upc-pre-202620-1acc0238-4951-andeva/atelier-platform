package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.exceptions.BusinessRuleValidationException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object representing an immutable date interval bounded by valid start and end dates.
 *
 * @author Joel Huamani Estefanero
 */
public record DateRange(LocalDate startDate, LocalDate endDate) {
    public DateRange {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleValidationException(
                    "INVALID_DATE_RANGE",
                    String.format("End date cannot be earlier than start date: %s < %s", endDate, startDate));
        }
    }

    public static DateRange of(LocalDate start, LocalDate end) {
        return new DateRange(start, end);
    }

    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "Date to check cannot be null");
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean overlaps(DateRange other) {
        Objects.requireNonNull(other, "Date range to compare cannot be null");
        return !this.endDate.isBefore(other.startDate) && !this.startDate.isAfter(other.endDate);
    }
}
