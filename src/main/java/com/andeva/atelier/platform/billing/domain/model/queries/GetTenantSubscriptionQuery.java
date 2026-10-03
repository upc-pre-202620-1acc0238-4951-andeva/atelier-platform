package com.andeva.atelier.platform.billing.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Query to retrieve the current subscription contract and status of a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record GetTenantSubscriptionQuery(
        TenantId tenantId
) {

    public GetTenantSubscriptionQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
    }
}
