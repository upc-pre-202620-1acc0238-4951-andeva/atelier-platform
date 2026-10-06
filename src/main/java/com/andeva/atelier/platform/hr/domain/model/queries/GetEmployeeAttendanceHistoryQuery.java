package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalDate;
import java.util.Objects;

public record GetEmployeeAttendanceHistoryQuery(
        TenantId tenantId,
        TenantMembershipId membershipId,
        LocalDate startDate,
        LocalDate endDate
) {
    public GetEmployeeAttendanceHistoryQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
        Objects.requireNonNull(startDate, "startDate cannot be null");
        Objects.requireNonNull(endDate, "endDate cannot be null");
    }
}
