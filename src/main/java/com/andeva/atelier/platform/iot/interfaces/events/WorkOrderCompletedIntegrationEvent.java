package com.andeva.atelier.platform.iot.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event consumed from Workshop Operations (MRO) when a repair work order is completed.
 * Automatically resolves active DTC faults associated with the repaired vehicle.
 *
 * @param tenantId         workshop tenant identifier
 * @param vehicleId        target vehicle identifier
 * @param workOrderId      completed work order identifier
 * @param resolvedDtcCodes list of DTC codes repaired in the workshop bay
 * @param occurredOn       timestamp of work order completion
 * @author Joel Huamani Estefanero
 */
public record WorkOrderCompletedIntegrationEvent(
        UUID tenantId,
        UUID vehicleId,
        UUID workOrderId,
        List<String> resolvedDtcCodes,
        Instant occurredOn
) implements Serializable {
    public WorkOrderCompletedIntegrationEvent {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        resolvedDtcCodes = (resolvedDtcCodes != null) ? List.copyOf(resolvedDtcCodes) : Collections.emptyList();
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
