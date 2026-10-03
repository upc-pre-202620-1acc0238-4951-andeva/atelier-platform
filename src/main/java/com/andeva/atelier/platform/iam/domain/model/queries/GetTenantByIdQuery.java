package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Domain query to retrieve an automotive workshop Tenant by its identifier.
 *
 * @author Joel Huamani Estefanero
 */
public record GetTenantByIdQuery(
        TenantId tenantId
) {
    public GetTenantByIdQuery {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
    }
}
