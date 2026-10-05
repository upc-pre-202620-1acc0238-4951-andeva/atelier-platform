package com.andeva.atelier.platform.operations.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

public record GetWorkOrdersByTenantIdQuery(
        TenantId tenantId
) {
}
