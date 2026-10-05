package com.andeva.atelier.platform.crm.domain.model.events;

import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record AppointmentConfirmedEvent(
        AppointmentId appointmentId,
        CustomerId customerId,
        Instant scheduledAt,
        Instant occurredOn
) implements Serializable {

    public AppointmentConfirmedEvent {
        Objects.requireNonNull(appointmentId, "AppointmentId cannot be null");
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        Objects.requireNonNull(scheduledAt, "ScheduledAt cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static AppointmentConfirmedEvent of(AppointmentId appointmentId, CustomerId customerId, Instant scheduledAt) {
        return new AppointmentConfirmedEvent(appointmentId, customerId, scheduledAt, Instant.now());
    }
}
