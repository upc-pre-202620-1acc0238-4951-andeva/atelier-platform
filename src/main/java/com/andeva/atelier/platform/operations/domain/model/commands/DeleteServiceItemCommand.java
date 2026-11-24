package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

public record DeleteServiceItemCommand(ServiceId serviceId, TenantId tenantId) {
}
