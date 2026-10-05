package com.andeva.atelier.platform.crm.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record VehicleOwnershipId(UUID value) implements Serializable {

    public VehicleOwnershipId {
        Objects.requireNonNull(value, "VehicleOwnershipId value cannot be null");
    }

    public static VehicleOwnershipId generate() {
        return new VehicleOwnershipId(UUID.randomUUID());
    }

    public static VehicleOwnershipId of(UUID value) {
        return new VehicleOwnershipId(value);
    }
}
