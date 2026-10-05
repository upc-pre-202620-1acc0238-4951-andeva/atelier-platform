package com.andeva.atelier.platform.operations.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;

public record GetAvailableWorkBaysQuery(
        TenantId tenantId,
        BranchId branchId,
        BayType bayType
) {
}
