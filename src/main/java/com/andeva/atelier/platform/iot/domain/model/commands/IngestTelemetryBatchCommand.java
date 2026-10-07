package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Domain command to ingest and persist a bulk batch of vehicle telemetry readings into TimescaleDB.
 *
 * @author Joel Huamani Estefanero
 */
public record IngestTelemetryBatchCommand(
        VehicleId vehicleId,
        TenantId tenantId,
        List<TelemetryRecord> readings
) implements Serializable {

    public IngestTelemetryBatchCommand {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        readings = readings != null ? List.copyOf(readings) : List.of();
    }
}
