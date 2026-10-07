package com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.repositories;

import com.andeva.atelier.platform.iot.domain.model.dto.TelemetryStatisticalSummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Analytical repository for querying aggregated statistical summaries from TimescaleDB {@code telemetry_logs}.
 * Uses {@link JdbcTemplate} to perform flexible temporal rollups.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public class TimescaleTelemetryAnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public TimescaleTelemetryAnalyticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Extracts aggregated daily telemetry metrics for a given vehicle since a specific timestamp.
     *
     * @param vehicleId target vehicle UUID
     * @param since     temporal cutoff threshold
     * @return list of daily statistical summaries
     */
    public List<TelemetryStatisticalSummary> getTelemetryMetricsDaily(UUID vehicleId, Instant since) {
        String sql = """
                SELECT 
                    COUNT(*) AS records_count,
                    COALESCE(AVG(speed), 0.0) AS avg_speed,
                    COALESCE(MAX(speed), 0.0) AS max_speed,
                    COALESCE(AVG(engine_temp_c), 0.0) AS avg_engine_temp,
                    COALESCE(MAX(engine_temp_c), 0.0) AS max_engine_temp,
                    COALESCE(AVG(engine_rpm), 0) AS avg_rpm,
                    COALESCE(MAX(engine_rpm), 0) AS max_rpm,
                    COALESCE(MIN(battery_voltage), 0.0) AS min_battery_voltage,
                    MIN(timestamp) AS bucket_start,
                    MAX(timestamp) AS bucket_end
                FROM telemetry_logs
                WHERE vehicle_id = ? AND timestamp >= ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int count = rs.getInt("records_count");
            if (count == 0) {
                return null;
            }
            Timestamp startTs = rs.getTimestamp("bucket_start");
            Timestamp endTs = rs.getTimestamp("bucket_end");
            Instant bucketStart = startTs != null ? startTs.toInstant() : since;
            Instant bucketEnd = endTs != null ? endTs.toInstant() : Instant.now();

            return new TelemetryStatisticalSummary(
                    count,
                    rs.getDouble("avg_speed"),
                    rs.getDouble("max_speed"),
                    rs.getDouble("avg_engine_temp"),
                    rs.getDouble("max_engine_temp"),
                    rs.getInt("avg_rpm"),
                    rs.getInt("max_rpm"),
                    rs.getDouble("min_battery_voltage"),
                    bucketStart,
                    bucketEnd
            );
        }, vehicleId, Timestamp.from(since)).stream()
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
