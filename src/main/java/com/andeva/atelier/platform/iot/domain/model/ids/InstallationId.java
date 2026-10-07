package com.andeva.atelier.platform.iot.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a physical OBD-II Device Installation session on a vehicle.
 *
 * @author Joel Huamani Estefanero
 */
public record InstallationId(UUID value) implements Serializable {

    public InstallationId {
        Objects.requireNonNull(value, "InstallationId value cannot be null");
    }

    public static InstallationId of(UUID value) {
        return new InstallationId(value);
    }

    public static InstallationId of(String value) {
        Objects.requireNonNull(value, "InstallationId string cannot be null");
        return new InstallationId(UUID.fromString(value));
    }

    public static InstallationId generate() {
        return new InstallationId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
