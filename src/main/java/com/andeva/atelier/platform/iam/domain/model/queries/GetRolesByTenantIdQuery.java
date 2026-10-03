package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Domain query to retrieve all security roles configured for a workshop Tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record GetRolesByTenantIdQuery(
        TenantId tenantId
) {
    public GetRolesByTenantIdQuery {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
    }
}
