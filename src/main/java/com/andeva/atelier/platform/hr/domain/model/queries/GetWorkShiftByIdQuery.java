package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetWorkShiftByIdQuery(TenantId tenantId, WorkShiftId shiftId) {
    public GetWorkShiftByIdQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(shiftId, "WorkShiftId cannot be null");
    }
}
