package com.andeva.atelier.platform.crm.domain.model.events;

import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record AppointmentArrivedEvent(
        AppointmentId appointmentId,
        TenantId tenantId,
        CustomerId customerId,
        VehicleId vehicleId,
        Instant occurredOn
) implements Serializable {

    public AppointmentArrivedEvent {
        Objects.requireNonNull(appointmentId, "AppointmentId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static AppointmentArrivedEvent of(AppointmentId appointmentId, TenantId tenantId, CustomerId customerId, VehicleId vehicleId) {
        return new AppointmentArrivedEvent(appointmentId, tenantId, customerId, vehicleId, Instant.now());
    }
}
