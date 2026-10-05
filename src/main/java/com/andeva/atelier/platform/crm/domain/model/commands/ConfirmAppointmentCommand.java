package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record ConfirmAppointmentCommand(TenantId tenantId, AppointmentId appointmentId) {
    public ConfirmAppointmentCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(appointmentId, "AppointmentId cannot be null");
    }
}
