package com.andeva.atelier.platform.hr.interfaces.rest.resources.requests;

import java.time.Instant;
import java.util.UUID;

public record ClockOutRequest(
        UUID attendanceId,
        Instant clockOutTime
) {}
