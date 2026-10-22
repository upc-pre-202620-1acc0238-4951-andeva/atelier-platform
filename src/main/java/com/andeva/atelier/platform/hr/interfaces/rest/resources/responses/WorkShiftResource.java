package com.andeva.atelier.platform.hr.interfaces.rest.resources.responses;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * @param workDays days the shift is worked, Monday first
 * @param restDays the remaining days of the week
 */
public record WorkShiftResource(
        UUID id,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        int gracePeriodMinutes,
        boolean isActive,
        List<String> workDays,
        List<String> restDays
) {}
