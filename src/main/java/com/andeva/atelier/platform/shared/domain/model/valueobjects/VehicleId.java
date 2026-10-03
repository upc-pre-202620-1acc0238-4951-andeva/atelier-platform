package com.andeva.atelier.platform.shared.domain.model.valueobjects;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for the automotive Vehicle.
 *
 * @author Joel Huamani Estefanero
 */
public record VehicleId(UUID value) implements Serializable {
    public VehicleId {
        Objects.requireNonNull(value, "Vehicle identifier cannot be null");
    }

    public static VehicleId of(UUID value) {
        return new VehicleId(value);
    }

    public static VehicleId of(String value) {
        Objects.requireNonNull(value, "Vehicle identifier cannot be null");
        return new VehicleId(UUID.fromString(value));
    }

    public static VehicleId generate() {
        return new VehicleId(UUID.randomUUID());
    }
}
