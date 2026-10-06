package com.andeva.atelier.platform.hr.interfaces.events;

import java.time.Instant;
import java.util.UUID;

public record MechanicClockedInIntegrationEvent(
        UUID attendanceId,
        UUID tenantId,
        UUID branchId,
        UUID membershipId,
        Instant clockIn,
        Instant occurredOn
) {}
