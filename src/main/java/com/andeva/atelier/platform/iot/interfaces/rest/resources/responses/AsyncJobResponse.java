package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing an asynchronous background job submitted for batch processing.
 *
 * @author Joel Huamani Estefanero
 */
public record AsyncJobResponse(
        UUID jobId,
        String status,
        UUID vehicleId,
        Instant submittedAt,
        Instant estimatedCompletionTime
) implements Serializable {
}
