package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Objects;

/**
 * Domain query to retrieve a staff membership contract for a specific tenant and user pair.
 *
 * @author Joel Huamani Estefanero
 */
public record GetMembershipByTenantAndUserQuery(
        TenantId tenantId,
        UserId userId
) {
    public GetMembershipByTenantAndUserQuery {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(userId, "User identifier cannot be null");
    }
}
