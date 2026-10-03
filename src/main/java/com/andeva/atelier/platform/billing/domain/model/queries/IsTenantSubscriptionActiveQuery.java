package com.andeva.atelier.platform.billing.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * High-speed query to determine whether a workshop tenant holds an active subscription or valid grace period.
 *
 * @author Joel Huamani Estefanero
 */
public record IsTenantSubscriptionActiveQuery(
        TenantId tenantId
) {

    public IsTenantSubscriptionActiveQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
    }
}
