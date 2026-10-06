package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import java.io.Serializable;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record EmployeeProfileRegisteredEvent(
        EmployeeProfileId employeeProfileId,
        TenantId tenantId,
        BranchId branchId,
        TenantMembershipId membershipId,
        Instant occurredOn
) implements Serializable {

    public EmployeeProfileRegisteredEvent {
        Objects.requireNonNull(employeeProfileId, "employeeProfileId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(branchId, "branchId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static EmployeeProfileRegisteredEvent now(
            EmployeeProfileId employeeProfileId,
            TenantId tenantId,
            BranchId branchId,
            TenantMembershipId membershipId
    ) {
        return new EmployeeProfileRegisteredEvent(employeeProfileId, tenantId, branchId, membershipId, Instant.now());
    }
}
