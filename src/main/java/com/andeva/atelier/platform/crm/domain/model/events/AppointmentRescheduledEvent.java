package com.andeva.atelier.platform.crm.domain.model.events;

import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record AppointmentRescheduledEvent(
        AppointmentId appointmentId,
        Instant newScheduledAt,
        Instant occurredOn
) implements Serializable {

    public AppointmentRescheduledEvent {
        Objects.requireNonNull(appointmentId, "AppointmentId cannot be null");
        Objects.requireNonNull(newScheduledAt, "NewScheduledAt cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static AppointmentRescheduledEvent of(AppointmentId appointmentId, Instant newScheduledAt) {
        return new AppointmentRescheduledEvent(appointmentId, newScheduledAt, Instant.now());
    }
}
