package com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.adapters;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.TimescaleBatchJdbcClientPort;
import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.dto.TelemetryStatisticalSummary;
import com.andeva.atelier.platform.iot.domain.repositories.TelemetryLogRepository;
import com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.assemblers.TimescaleTelemetryPersistenceAssembler;
import com.andeva.atelier.platform.iot.infrastructure.persistence.timescale.entities.TelemetryLogPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary adapter implementing both {@link TelemetryLogRepository} and {@link TimescaleBatchJdbcClientPort}
 * using direct {@link JdbcTemplate} batch operations against the TimescaleDB {@code telemetry_logs} hypertable.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public class TimescaleTelemetryRepositoryImpl implements TelemetryLogRepository, TimescaleBatchJdbcClientPort {

    private final JdbcTemplate jdbcTemplate;
    private final TimescaleTelemetryPersistenceAssembler assembler;

    public TimescaleTelemetryRepositoryImpl(
            JdbcTemplate jdbcTemplate,
            TimescaleTelemetryPersistenceAssembler assembler) {
        this.jdbcTemplate = jdbcTemplate;
        this.assembler = assembler;
    }

    @Override
    public void executeBatchInsert(List<TelemetryRecord> records) {
        saveAllBatch(records);
    }

    @Override
    public void saveAllBatch(List<TelemetryRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO telemetry_logs (
                    timestamp, vehicle_id, tenant_id, latitude, longitude,
                    speed, engine_temp_c, engine_rpm, fuel_level_pct, battery_voltage
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, records, records.size(), (PreparedStatement ps, TelemetryRecord record) -> {
            ps.setTimestamp(1, Timestamp.from(record.timestamp()));
            ps.setObject(2, record.vehicleId().value());
            ps.setObject(3, record.tenantId().value());
            ps.setObject(4, record.location().map(l -> l.latitude()).orElse(null));
            ps.setObject(5, record.location().map(l -> l.longitude()).orElse(null));
            ps.setInt(6, record.speed().kmh());
            ps.setDouble(7, record.engineTemperature().celsius());
            ps.setInt(8, record.engineRpm().rpm());
            ps.setObject(9, record.fuelLevel().map(f -> f.percentage()).orElse(null));
            ps.setObject(10, record.batteryVoltage().map(b -> b.volts()).orElse(null));
        });
    }

    @Override
    public Optional<TelemetryRecord> findLatestByVehicleId(VehicleId vehicleId) {
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");

        String sql = """
                SELECT timestamp, vehicle_id, tenant_id, latitude, longitude,
                       speed, engine_temp_c, engine_rpm, fuel_level_pct, battery_voltage
                FROM telemetry_logs
                WHERE vehicle_id = ?
                ORDER BY timestamp DESC
                LIMIT 1
                """;

        List<TelemetryLogPersistenceEntity> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            var entity = new TelemetryLogPersistenceEntity();
            entity.setId(new TelemetryLogPersistenceEntity.TelemetryRecordId(
                    rs.getTimestamp("timestamp").toInstant(),
                    (UUID) rs.getObject("vehicle_id")
            ));
            entity.setTenantId((UUID) rs.getObject("tenant_id"));
            entity.setLatitude((Double) rs.getObject("latitude"));
            entity.setLongitude((Double) rs.getObject("longitude"));
            entity.setSpeed(rs.getInt("speed"));
            entity.setEngineTempC(rs.getDouble("engine_temp_c"));
            entity.setEngineRpm(rs.getInt("engine_rpm"));
            entity.setFuelLevelPct((Double) rs.getObject("fuel_level_pct"));
            entity.setBatteryVoltage((Double) rs.getObject("battery_voltage"));
            return entity;
        }, vehicleId.value());

        return results.isEmpty() ? Optional.empty() : Optional.of(assembler.toDomain(results.get(0)));
    }

    @Override
    public List<TelemetryRecord> findHistoryAggregated(VehicleId vehicleId, Instant from, Instant to, String timeBucket) {
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(to, "to cannot be null");

        String sql = """
                SELECT timestamp, vehicle_id, tenant_id, latitude, longitude,
                       speed, engine_temp_c, engine_rpm, fuel_level_pct, battery_voltage
                FROM telemetry_logs
                WHERE vehicle_id = ? AND timestamp >= ? AND timestamp <= ?
                ORDER BY timestamp ASC
                """;

        List<TelemetryLogPersistenceEntity> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            var entity = new TelemetryLogPersistenceEntity();
            entity.setId(new TelemetryLogPersistenceEntity.TelemetryRecordId(
                    rs.getTimestamp("timestamp").toInstant(),
                    (UUID) rs.getObject("vehicle_id")
            ));
            entity.setTenantId((UUID) rs.getObject("tenant_id"));
            entity.setLatitude((Double) rs.getObject("latitude"));
            entity.setLongitude((Double) rs.getObject("longitude"));
            entity.setSpeed(rs.getInt("speed"));
            entity.setEngineTempC(rs.getDouble("engine_temp_c"));
            entity.setEngineRpm(rs.getInt("engine_rpm"));
            entity.setFuelLevelPct((Double) rs.getObject("fuel_level_pct"));
            entity.setBatteryVoltage((Double) rs.getObject("battery_voltage"));
            return entity;
        }, vehicleId.value(), Timestamp.from(from), Timestamp.from(to));

        return results.stream().map(assembler::toDomain).toList();
    }

    @Override
    public Optional<TelemetryStatisticalSummary> calculateStatisticalSummary(VehicleId vehicleId, Instant from, Instant to) {
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(to, "to cannot be null");

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
                WHERE vehicle_id = ? AND timestamp >= ? AND timestamp <= ?
                """;

        List<TelemetryStatisticalSummary> summaries = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int count = rs.getInt("records_count");
            if (count == 0) {
                return null;
            }
            Timestamp startTs = rs.getTimestamp("bucket_start");
            Timestamp endTs = rs.getTimestamp("bucket_end");
            Instant bucketStart = startTs != null ? startTs.toInstant() : from;
            Instant bucketEnd = endTs != null ? endTs.toInstant() : to;

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
        }, vehicleId.value(), Timestamp.from(from), Timestamp.from(to));

        return summaries.stream().filter(Objects::nonNull).findFirst();
    }
}
