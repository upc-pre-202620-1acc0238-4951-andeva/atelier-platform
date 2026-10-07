package com.andeva.atelier.platform.iot.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a recorded Vehicle DTC Fault incident.
 *
 * @author Joel Huamani Estefanero
 */
public record FaultId(UUID value) implements Serializable {

    public FaultId {
        Objects.requireNonNull(value, "FaultId value cannot be null");
    }

    public static FaultId of(UUID value) {
        return new FaultId(value);
    }

    public static FaultId of(String value) {
        Objects.requireNonNull(value, "FaultId string cannot be null");
        return new FaultId(UUID.fromString(value));
    }

    public static FaultId generate() {
        return new FaultId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
