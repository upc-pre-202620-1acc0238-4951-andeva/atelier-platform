package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record ListEmployeeProfilesByBranchQuery(TenantId tenantId, BranchId branchId) {
    public ListEmployeeProfilesByBranchQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
    }
}
