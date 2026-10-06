package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record ActivateWorkShiftCommand(
        TenantId tenantId,
        WorkShiftId workShiftId
) {
    public ActivateWorkShiftCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(workShiftId, "WorkShiftId cannot be null");
    }
}
