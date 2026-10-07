package com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Persistence entity mapped to the {@code telemetry_logs} hypertable in TimescaleDB.
 * Utilizes a composite primary key composed of {@code (timestamp, vehicle_id)} for
 * hypertable time-range partitioning.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "telemetry_logs")
public class TelemetryLogPersistenceEntity {

    @EmbeddedId
    private TelemetryRecordId id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "speed", nullable = false)
    private int speed;

    @Column(name = "engine_temp_c", nullable = false)
    private double engineTempC;

    @Column(name = "engine_rpm", nullable = false)
    private int engineRpm;

    @Column(name = "fuel_level_pct")
    private Double fuelLevelPct;

    @Column(name = "battery_voltage")
    private Double batteryVoltage;

    /**
     * Composite primary key for TimescaleDB hypertable telemetry log records.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class TelemetryRecordId implements Serializable {

        @Column(name = "timestamp", nullable = false)
        private Instant timestamp;

        @Column(name = "vehicle_id", nullable = false)
        private UUID vehicleId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TelemetryRecordId that)) return false;
            return Objects.equals(timestamp, that.timestamp) && Objects.equals(vehicleId, that.vehicleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, vehicleId);
        }
    }
}
