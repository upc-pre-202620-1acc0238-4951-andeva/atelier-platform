package com.andeva.atelier.platform.hr.domain;

import com.andeva.atelier.platform.hr.domain.exceptions.AttendanceNotJustifiableException;
import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.enums.AttendanceStatus;
import com.andeva.atelier.platform.hr.domain.model.enums.JustificationStatus;
import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.HaversineDistance;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Attendance justification submitted by the employee and reviewed by the administration (US10)")
class AttendanceJustificationReviewTest {

    private final TenantMembershipId employee = TenantMembershipId.of(UUID.randomUUID());
    private final TenantMembershipId supervisor = TenantMembershipId.of(UUID.randomUUID());

    private AttendanceRecord record(AttendanceStatus status) {
        return new AttendanceRecord(AttendanceRecordId.generate(), TenantId.of(UUID.randomUUID()), BranchId.of(UUID.randomUUID()),
                employee, WorkShiftId.generate(), Instant.now(), null, status,
                GeoCoordinates.of(-12.05, -77.04), HaversineDistance.of(20), null);
    }

    @Test
    @DisplayName("Approving the submitted reason excuses the attendance")
    void shouldExcuseOnApproval() {
        AttendanceRecord late = record(AttendanceStatus.LATE);

        late.submitJustification("Accidente en la vía", employee);
        assertThat(late.getJustificationStatus()).contains(JustificationStatus.PENDING);
        assertThat(late.getStatus()).isEqualTo(AttendanceStatus.LATE);

        late.approveJustification(supervisor);
        assertThat(late.getStatus()).isEqualTo(AttendanceStatus.EXCUSED);
        assertThat(late.getJustificationStatus()).contains(JustificationStatus.APPROVED);
        assertThat(late.getJustificationReviewedBy()).contains(supervisor);
    }

    @Test
    @DisplayName("Rejecting keeps the late status and allows a new submission")
    void shouldKeepLateOnRejection() {
        AttendanceRecord late = record(AttendanceStatus.LATE);
        late.submitJustification("Tráfico", employee);

        late.rejectJustification(supervisor);
        assertThat(late.getStatus()).isEqualTo(AttendanceStatus.LATE);
        assertThat(late.getJustificationStatus()).contains(JustificationStatus.REJECTED);

        late.submitJustification("Constancia médica adjunta", employee);
        assertThat(late.getJustificationStatus()).contains(JustificationStatus.PENDING);
    }

    @Test
    @DisplayName("Only the record owner can submit, and punctual records cannot be justified")
    void shouldGuardSubmission() {
        assertThatThrownBy(() -> record(AttendanceStatus.LATE).submitJustification("x", supervisor))
                .isInstanceOf(AttendanceNotJustifiableException.class);
        assertThatThrownBy(() -> record(AttendanceStatus.ON_TIME).submitJustification("x", employee))
                .isInstanceOf(AttendanceNotJustifiableException.class);
        assertThatThrownBy(() -> record(AttendanceStatus.LATE).approveJustification(supervisor))
                .isInstanceOf(AttendanceNotJustifiableException.class);
    }
}
