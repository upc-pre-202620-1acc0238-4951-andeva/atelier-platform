package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalDate;
import java.util.Objects;

public record ListAttendanceByBranchAndDateQuery(TenantId tenantId, BranchId branchId, LocalDate date) {
    public ListAttendanceByBranchAndDateQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
    }
}
