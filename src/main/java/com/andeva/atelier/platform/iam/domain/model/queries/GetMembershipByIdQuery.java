package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;

import java.util.Objects;

/**
 * Domain query to retrieve a staff membership contract by its unique identifier.
 *
 * @author Joel Huamani Estefanero
 */
public record GetMembershipByIdQuery(
        TenantMembershipId membershipId
) {
    public GetMembershipByIdQuery {
        Objects.requireNonNull(membershipId, "Membership identifier cannot be null");
    }
}
