package com.andeva.atelier.platform.operations.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

public record GetWorkBaysByBranchIdQuery(
        TenantId tenantId,
        BranchId branchId
) {
}
