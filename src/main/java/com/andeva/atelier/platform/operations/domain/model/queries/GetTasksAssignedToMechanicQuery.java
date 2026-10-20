package com.andeva.atelier.platform.operations.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;
import java.util.UUID;

/**
 * Query for the open tasks assigned to the authenticated mechanic (user account) in a tenant.
 */
public record GetTasksAssignedToMechanicQuery(
        TenantId tenantId,
        UUID mechanicUserId
) {
    public GetTasksAssignedToMechanicQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(mechanicUserId, "mechanicUserId cannot be null");
    }
}
