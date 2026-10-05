package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;

public record CreateWorkBayCommand(
        TenantId tenantId,
        BranchId branchId,
        String name,
        BayType bayType
) {
}
