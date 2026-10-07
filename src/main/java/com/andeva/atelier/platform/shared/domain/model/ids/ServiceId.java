package com.andeva.atelier.platform.shared.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for automotive Workshop Services (Shared Kernel ID).
 * Compatible alias matching canonical package documentation.
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

    public com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId toValueObject() {
        return new com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId(value);
    }

    public static ServiceId fromValueObject(com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId serviceId) {
        Objects.requireNonNull(serviceId, "ServiceId cannot be null");
        return new ServiceId(serviceId.value());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
