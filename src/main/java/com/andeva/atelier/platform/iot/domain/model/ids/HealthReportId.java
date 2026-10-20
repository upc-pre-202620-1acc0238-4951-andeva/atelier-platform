package com.andeva.atelier.platform.iot.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for a persisted AI vehicle health report.
 *
 * @author Joel Huamani Estefanero
 */
public record HealthReportId(UUID value) implements Serializable {

    public HealthReportId {
        Objects.requireNonNull(value, "HealthReportId value cannot be null");
    }

    public static HealthReportId of(UUID value) {
        return new HealthReportId(value);
    }

    public static HealthReportId generate() {
        return new HealthReportId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
