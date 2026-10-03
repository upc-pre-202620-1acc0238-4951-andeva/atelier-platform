package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Domain query to retrieve all physical branches associated with a workshop Tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record GetBranchesByTenantIdQuery(
        TenantId tenantId
) {
    public GetBranchesByTenantIdQuery {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
    }
}
