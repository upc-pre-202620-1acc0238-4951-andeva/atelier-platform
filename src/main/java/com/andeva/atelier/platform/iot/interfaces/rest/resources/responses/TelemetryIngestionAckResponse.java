package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.util.UUID;

/**
 * REST response acknowledging the asynchronous batch ingestion of telemetry into TimescaleDB.
 *
 * @author Joel Huamani Estefanero
 */
public record TelemetryIngestionAckResponse(
        UUID vehicleId,
        int ingestedCount,
        boolean anomalyDetected,
        String alertMessage
) implements Serializable {
}
