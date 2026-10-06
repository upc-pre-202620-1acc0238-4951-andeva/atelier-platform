package com.andeva.atelier.platform.hr.interfaces.rest.resources.responses;

import java.time.Instant;
import java.util.UUID;

public record AttendanceResource(
        UUID id,
        UUID branchId,
        UUID membershipId,
        UUID shiftId,
        Instant clockIn,
        Instant clockOut,
        String status,
        Double latitude,
        Double longitude,
        Double distanceToBranchMeters,
        String justificationReason,
        UUID justifiedBy,
        Instant justifiedAt
) {}
