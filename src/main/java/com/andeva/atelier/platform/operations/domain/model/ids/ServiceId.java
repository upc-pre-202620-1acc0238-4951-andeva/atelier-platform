package com.andeva.atelier.platform.operations.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record ServiceId(UUID value) implements Serializable {

    public ServiceId {
        Objects.requireNonNull(value, "ServiceId value cannot be null");
    }

    public static ServiceId generate() {
        return new ServiceId(UUID.randomUUID());
    }

    public static ServiceId of(UUID value) {
        return new ServiceId(value);
    }
}
