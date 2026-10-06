package com.andeva.atelier.platform.hr.interfaces.acl.dto;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

public record EmployeeWorkShiftAclDto(
        UUID shiftId,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        int gracePeriodMinutes
) implements Serializable {}
