package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import java.io.Serializable;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record LateAttendanceRecordedEvent(
        AttendanceRecordId attendanceRecordId,
        TenantId tenantId,
        TenantMembershipId membershipId,
        long delayMinutes,
        Instant occurredOn
) implements Serializable {

    public LateAttendanceRecordedEvent {
        Objects.requireNonNull(attendanceRecordId, "attendanceRecordId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static LateAttendanceRecordedEvent now(
            AttendanceRecordId attendanceRecordId,
            TenantId tenantId,
            TenantMembershipId membershipId,
            long delayMinutes
    ) {
        return new LateAttendanceRecordedEvent(attendanceRecordId, tenantId, membershipId, delayMinutes, Instant.now());
    }
}
