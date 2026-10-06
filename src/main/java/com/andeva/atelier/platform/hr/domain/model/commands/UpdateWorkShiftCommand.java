package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalTime;
import java.util.Objects;

public record UpdateWorkShiftCommand(
        TenantId tenantId,
        WorkShiftId workShiftId,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        int gracePeriodMinutes
) {
    public UpdateWorkShiftCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(workShiftId, "WorkShiftId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(startTime, "startTime cannot be null");
        Objects.requireNonNull(endTime, "endTime cannot be null");
    }
}
