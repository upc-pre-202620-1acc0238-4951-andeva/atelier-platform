package com.andeva.atelier.platform.iot.application.commandservices;

import com.andeva.atelier.platform.iot.domain.model.commands.IngestTelemetryBatchCommand;

/**
 * Command Service for ingesting real-time vehicle telemetry batches into TimescaleDB
 * and performing real-time mathematical anomaly detection.
 *
 * @author Joel Huamani Estefanero
 */
public interface TelemetryIngestionCommandService {

    void handle(IngestTelemetryBatchCommand command);
}
