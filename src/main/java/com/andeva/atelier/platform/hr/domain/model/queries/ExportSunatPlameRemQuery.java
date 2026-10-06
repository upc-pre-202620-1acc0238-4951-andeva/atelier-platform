package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record ExportSunatPlameRemQuery(
        TenantId tenantId,
        String period,
        BranchId branchId
) {
    public ExportSunatPlameRemQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(period, "period cannot be null");
    }
}
