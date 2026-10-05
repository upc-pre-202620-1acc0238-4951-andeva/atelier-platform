package com.andeva.atelier.platform.crm.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public record VehicleOwnershipTransferredEvent(
        VehicleId vehicleId,
        CustomerId previousOwnerId,
        CustomerId newOwnerId,
        LocalDate transferDate,
        Instant occurredOn
) implements Serializable {

    public VehicleOwnershipTransferredEvent {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(newOwnerId, "NewOwnerId cannot be null");
        Objects.requireNonNull(transferDate, "TransferDate cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static VehicleOwnershipTransferredEvent of(VehicleId vehicleId, CustomerId previousOwnerId, CustomerId newOwnerId, LocalDate transferDate) {
        return new VehicleOwnershipTransferredEvent(vehicleId, previousOwnerId, newOwnerId, transferDate, Instant.now());
    }
}
