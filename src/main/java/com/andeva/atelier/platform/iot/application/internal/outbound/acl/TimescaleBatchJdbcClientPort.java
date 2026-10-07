package com.andeva.atelier.platform.iot.application.internal.outbound.acl;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;

import java.util.List;

/**
 * Outbound port for high-speed JDBC batch insertion of telemetry records into TimescaleDB.
 * Guarantees sub-25ms ingestion latency for batches up to 100 records.
 *
 * @author Joel Huamani Estefanero
 */
public interface TimescaleBatchJdbcClientPort {

    /**
     * Executes a batch insert into the {@code telemetry_logs} hypertable.
     *
     * @param records collection of immutable telemetry records
     */
    void executeBatchInsert(List<TelemetryRecord> records);
}
