package com.andeva.atelier.platform.crm.interfaces.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when vehicle legal custody or ownership is transferred.
 *
 * @param vehicleId       Identifier of the vehicle transferred
 * @param previousOwnerId Identifier of the prior owner
 * @param newOwnerId      Identifier of the recipient owner
 * @param transferDate    Calendar date of legal transfer
 * @param occurredOn      Timestamp when the event occurred
 * @author Adiel Sanchez Santin
 */
public record VehicleOwnershipTransferredIntegrationEvent(
        UUID vehicleId,
        UUID previousOwnerId,
        UUID newOwnerId,
        LocalDate transferDate,
        Instant occurredOn
) {
    public VehicleOwnershipTransferredIntegrationEvent {
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(newOwnerId, "newOwnerId cannot be null");
        Objects.requireNonNull(transferDate, "transferDate cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
