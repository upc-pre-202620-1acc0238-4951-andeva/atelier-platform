package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for automotive Workshop Services (Shared Kernel cross-cutting ID).
 * Represents maintenance operations, diagnostic packages, and labor items across MRO, Invoicing, and IoT.
 *
 * @author Joel Huamani Estefanero
 */
public record ServiceId(UUID value) implements Serializable {

    public ServiceId {
        Objects.requireNonNull(value, "Service identifier cannot be null");
    }

    public static ServiceId of(UUID value) {
        return new ServiceId(value);
    }

    public static ServiceId of(String value) {
        Objects.requireNonNull(value, "Service identifier cannot be null");
        return new ServiceId(UUID.fromString(value));
    }

    public static ServiceId generate() {
        return new ServiceId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
