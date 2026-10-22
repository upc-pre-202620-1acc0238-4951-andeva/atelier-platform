package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/**
 * @param workDays days the shift is worked (MONDAY…SUNDAY); omitted = every day
 */
public record CreateWorkShiftResource(
        @NotBlank @Size(max = 100) String name,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Min(0) @Max(60) int gracePeriodMinutes,
        Set<DayOfWeek> workDays
) {
    public CreateWorkShiftResource(String name, LocalTime startTime, LocalTime endTime, int gracePeriodMinutes) {
        this(name, startTime, endTime, gracePeriodMinutes, null);
    }
}
