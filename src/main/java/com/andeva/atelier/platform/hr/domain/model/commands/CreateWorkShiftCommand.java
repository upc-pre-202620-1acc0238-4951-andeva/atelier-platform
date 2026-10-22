package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

/**
 * @param workDays days the shift is worked; null or empty = every day
 */
public record CreateWorkShiftCommand(
        TenantId tenantId,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        int gracePeriodMinutes,
        Set<DayOfWeek> workDays
) {
    public CreateWorkShiftCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(startTime, "startTime cannot be null");
        Objects.requireNonNull(endTime, "endTime cannot be null");
    }

    public CreateWorkShiftCommand(TenantId tenantId, String name, LocalTime startTime, LocalTime endTime, int gracePeriodMinutes) {
        this(tenantId, name, startTime, endTime, gracePeriodMinutes, null);
    }
}
