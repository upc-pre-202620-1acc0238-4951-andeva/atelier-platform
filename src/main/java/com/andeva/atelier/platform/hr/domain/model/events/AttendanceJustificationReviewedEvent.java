package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.enums.JustificationStatus;
import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * The administration issued its verdict on an employee's justification.
 */
public record AttendanceJustificationReviewedEvent(
        AttendanceRecordId attendanceRecordId,
        TenantId tenantId,
        TenantMembershipId membershipId,
        TenantMembershipId reviewedBy,
        JustificationStatus verdict,
        Instant occurredOn
) implements Serializable {

    public AttendanceJustificationReviewedEvent {
        Objects.requireNonNull(attendanceRecordId, "attendanceRecordId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(verdict, "verdict cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static AttendanceJustificationReviewedEvent now(AttendanceRecordId attendanceRecordId, TenantId tenantId,
            TenantMembershipId membershipId, TenantMembershipId reviewedBy, JustificationStatus verdict) {
        return new AttendanceJustificationReviewedEvent(attendanceRecordId, tenantId, membershipId, reviewedBy, verdict, Instant.now());
    }
}
