package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import java.io.Serializable;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record AttendanceJustifiedEvent(
        AttendanceRecordId attendanceRecordId,
        TenantId tenantId,
        TenantMembershipId membershipId,
        TenantMembershipId justifiedBy,
        String reason,
        Instant occurredOn
) implements Serializable {

    public AttendanceJustifiedEvent {
        Objects.requireNonNull(attendanceRecordId, "attendanceRecordId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(justifiedBy, "justifiedBy cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static AttendanceJustifiedEvent now(
            AttendanceRecordId attendanceRecordId,
            TenantId tenantId,
            TenantMembershipId membershipId,
            TenantMembershipId justifiedBy,
            String reason
    ) {
        return new AttendanceJustifiedEvent(attendanceRecordId, tenantId, membershipId, justifiedBy, reason, Instant.now());
    }
}
