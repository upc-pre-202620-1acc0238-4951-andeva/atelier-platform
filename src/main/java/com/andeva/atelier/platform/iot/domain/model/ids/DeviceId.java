package com.andeva.atelier.platform.iot.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for an OBD-II Telemetry Device.
 *
 * @author Joel Huamani Estefanero
 */
public record DeviceId(UUID value) implements Serializable {

    public DeviceId {
        Objects.requireNonNull(value, "DeviceId value cannot be null");
    }

    public static DeviceId of(UUID value) {
        return new DeviceId(value);
    }

    public static DeviceId of(String value) {
        Objects.requireNonNull(value, "DeviceId string cannot be null");
        return new DeviceId(UUID.fromString(value));
    }

    public static DeviceId generate() {
        return new DeviceId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
