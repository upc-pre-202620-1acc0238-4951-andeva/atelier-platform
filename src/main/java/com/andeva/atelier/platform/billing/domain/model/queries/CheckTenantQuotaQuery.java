package com.andeva.atelier.platform.billing.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Query to inspect effective quota limits and active feature flags for a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record CheckTenantQuotaQuery(
        TenantId tenantId
) {

    public CheckTenantQuotaQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
    }
}
