package com.andeva.atelier.platform.crm.domain.model.events;

import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record VehicleRegisteredEvent(
        VehicleId vehicleId,
        LicensePlate plate,
        CustomerId initialOwnerId,
        Instant occurredOn
) implements Serializable {

    public VehicleRegisteredEvent {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(plate, "LicensePlate cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static VehicleRegisteredEvent of(VehicleId vehicleId, LicensePlate plate, CustomerId initialOwnerId) {
        return new VehicleRegisteredEvent(vehicleId, plate, initialOwnerId, Instant.now());
    }
}
