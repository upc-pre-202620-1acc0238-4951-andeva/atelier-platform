package com.andeva.atelier.platform.hr.interfaces.rest.resources.responses;

import java.time.LocalTime;
import java.util.UUID;

public record WorkShiftResource(
        UUID id,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        int gracePeriodMinutes,
        boolean isActive
) {}
