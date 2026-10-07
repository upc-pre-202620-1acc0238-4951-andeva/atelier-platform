package com.andeva.atelier.platform.iot.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event consumed from CRM & Fleet when a vehicle is permanently decommissioned or scrapped.
 * Triggers automatic uninstallation of any active OBD-II devices.
 *
 * @param tenantId   workshop tenant identifier
 * @param vehicleId  decommissioned vehicle identifier
 * @param occurredOn timestamp of decommissioning
 * @author Joel Huamani Estefanero
 */
public record VehicleDecommissionedIntegrationEvent(
        UUID tenantId,
        UUID vehicleId,
        Instant occurredOn
) implements Serializable {
    public VehicleDecommissionedIntegrationEvent {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
