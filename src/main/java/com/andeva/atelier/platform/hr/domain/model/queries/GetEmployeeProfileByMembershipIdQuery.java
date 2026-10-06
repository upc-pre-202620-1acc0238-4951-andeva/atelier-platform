package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetEmployeeProfileByMembershipIdQuery(TenantId tenantId, TenantMembershipId membershipId) {
    public GetEmployeeProfileByMembershipIdQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
    }
}
