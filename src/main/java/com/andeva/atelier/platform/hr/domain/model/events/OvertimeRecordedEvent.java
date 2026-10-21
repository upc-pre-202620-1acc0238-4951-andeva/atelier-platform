package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import java.io.Serializable;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

/**
 * A closed day ran past the end of the shift beyond the tolerance: the excess is overtime for payroll.
 */
public record OvertimeRecordedEvent(
        AttendanceRecordId attendanceRecordId,
        TenantId tenantId,
        TenantMembershipId membershipId,
        long overtimeMinutes,
        Instant occurredOn
) implements Serializable {

    public OvertimeRecordedEvent {
        Objects.requireNonNull(attendanceRecordId, "attendanceRecordId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static OvertimeRecordedEvent now(
            AttendanceRecordId attendanceRecordId,
            TenantId tenantId,
            TenantMembershipId membershipId,
            long overtimeMinutes
    ) {
        return new OvertimeRecordedEvent(attendanceRecordId, tenantId, membershipId, overtimeMinutes, Instant.now());
    }
}
