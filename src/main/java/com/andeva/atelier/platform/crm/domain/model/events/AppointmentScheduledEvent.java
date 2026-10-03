package com.andeva.atelier.platform.crm.domain.model.events;

import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record AppointmentScheduledEvent(
        AppointmentId appointmentId,
        TenantId tenantId,
        BranchId branchId,
        CustomerId customerId,
        VehicleId vehicleId,
        Instant scheduledAt,
        Instant occurredOn
) implements Serializable {

    public AppointmentScheduledEvent {
        Objects.requireNonNull(appointmentId, "AppointmentId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(scheduledAt, "ScheduledAt cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static AppointmentScheduledEvent of(
            AppointmentId appointmentId,
            TenantId tenantId,
            BranchId branchId,
            CustomerId customerId,
            VehicleId vehicleId,
            Instant scheduledAt
    ) {
        return new AppointmentScheduledEvent(appointmentId, tenantId, branchId, customerId, vehicleId, scheduledAt, Instant.now());
    }
}
