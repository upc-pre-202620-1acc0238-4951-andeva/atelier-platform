package com.andeva.atelier.platform.iot.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event consumed from CRM & Fleet when vehicle ownership is transferred.
 * Triggers uninstallation of existing devices and resets baseline analytical metrics.
 *
 * @param tenantId           workshop tenant identifier
 * @param vehicleId          target vehicle identifier
 * @param previousCustomerId previous customer identifier
 * @param newCustomerId      new customer identifier
 * @param occurredOn         timestamp of transfer
 * @author Joel Huamani Estefanero
 */
public record VehicleOwnershipTransferredIntegrationEvent(
        UUID tenantId,
        UUID vehicleId,
        UUID previousCustomerId,
        UUID newCustomerId,
        Instant occurredOn
) implements Serializable {
    public VehicleOwnershipTransferredIntegrationEvent {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(previousCustomerId, "previousCustomerId cannot be null");
        Objects.requireNonNull(newCustomerId, "newCustomerId cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
