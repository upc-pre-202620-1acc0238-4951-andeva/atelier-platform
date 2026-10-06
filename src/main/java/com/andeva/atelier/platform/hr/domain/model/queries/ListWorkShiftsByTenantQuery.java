package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record ListWorkShiftsByTenantQuery(TenantId tenantId) {
    public ListWorkShiftsByTenantQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
    }
}
