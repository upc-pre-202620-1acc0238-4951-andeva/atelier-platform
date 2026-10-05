package com.andeva.atelier.platform.crm.domain.model.queries;

import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

public record GetAppointmentByIdQuery(TenantId tenantId, AppointmentId appointmentId) {
}
