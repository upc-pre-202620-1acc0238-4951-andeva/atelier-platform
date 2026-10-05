package com.andeva.atelier.platform.crm.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record AppointmentId(UUID value) implements Serializable {

    public AppointmentId {
        Objects.requireNonNull(value, "AppointmentId value cannot be null");
    }

    public static AppointmentId generate() {
        return new AppointmentId(UUID.randomUUID());
    }

    public static AppointmentId of(UUID value) {
        return new AppointmentId(value);
    }
}
