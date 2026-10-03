package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Domain query to retrieve all staff memberships established under a workshop Tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record GetMembershipsByTenantIdQuery(
        TenantId tenantId
) {
    public GetMembershipsByTenantIdQuery {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
    }
}
