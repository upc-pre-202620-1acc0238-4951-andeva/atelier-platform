package com.andeva.atelier.platform.hr.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record AttendanceJustification(
        String reason,
        TenantMembershipId justifiedBy,
        Instant justifiedAt
) implements Serializable {

    public AttendanceJustification {
        Objects.requireNonNull(reason, "reason cannot be null");
        Objects.requireNonNull(justifiedBy, "justifiedBy cannot be null");
        Objects.requireNonNull(justifiedAt, "justifiedAt cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Justification reason cannot be blank");
        }
    }

    public static AttendanceJustification of(String reason, TenantMembershipId justifiedBy, Instant justifiedAt) {
        return new AttendanceJustification(reason, justifiedBy, justifiedAt);
    }
}
