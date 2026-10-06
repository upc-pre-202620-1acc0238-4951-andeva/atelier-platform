package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalTime;
import java.util.Objects;

public record CreateWorkShiftCommand(
        TenantId tenantId,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        int gracePeriodMinutes
) {
    public CreateWorkShiftCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(startTime, "startTime cannot be null");
        Objects.requireNonNull(endTime, "endTime cannot be null");
    }
}
