package com.andeva.atelier.platform.crm.interfaces.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a universal vehicle is registered into the platform.
 *
 * @param vehicleId  Universal identifier of the vehicle
 * @param plate      License plate string
 * @param vin        Vehicle Identification Number (ISO 3779)
 * @param brand      Vehicle make or brand
 * @param model      Vehicle model name
 * @param year       Manufacturing model year
 * @param engineType Engine powertrain type
 * @param ownerId    Identifier of the initial owner (customer or user)
 * @param occurredOn Timestamp when the event occurred
 * @author Adiel Sanchez Santin
 */
public record VehicleRegisteredIntegrationEvent(
        UUID vehicleId,
        String plate,
        String vin,
        String brand,
        String model,
        int year,
        String engineType,
        UUID ownerId,
        Instant occurredOn
) {
    public VehicleRegisteredIntegrationEvent {
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        Objects.requireNonNull(plate, "plate cannot be null");
        Objects.requireNonNull(brand, "brand cannot be null");
        Objects.requireNonNull(model, "model cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
