package com.andeva.atelier.platform.crm.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

public record GetAppointmentsByCustomerQuery(TenantId tenantId, CustomerId customerId) {
}
