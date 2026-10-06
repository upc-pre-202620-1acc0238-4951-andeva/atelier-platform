package com.andeva.atelier.platform.hr.interfaces.events;

import java.time.Instant;
import java.util.UUID;

public record MechanicClockedOutIntegrationEvent(
        UUID attendanceId,
        UUID tenantId,
        UUID membershipId,
        Instant clockOut,
        long totalWorkedMinutes,
        Instant occurredOn
) {}
