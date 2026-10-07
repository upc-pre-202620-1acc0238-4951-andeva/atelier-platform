package com.andeva.atelier.platform.iot.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a Predictive Maintenance Alert.
 *
 * @author Joel Huamani Estefanero
 */
public record AlertId(UUID value) implements Serializable {

    public AlertId {
        Objects.requireNonNull(value, "AlertId value cannot be null");
    }

    public static AlertId of(UUID value) {
        return new AlertId(value);
    }

    public static AlertId of(String value) {
        Objects.requireNonNull(value, "AlertId string cannot be null");
        return new AlertId(UUID.fromString(value));
    }

    public static AlertId generate() {
        return new AlertId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
