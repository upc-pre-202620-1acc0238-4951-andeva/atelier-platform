package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.hr.domain.model.enums.AttendanceStatus;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.converters.AttendanceStatusAttributeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "attendance_records",
        indexes = {
                @Index(name = "idx_attendance_membership_date", columnList = "membership_id, clock_in"),
                @Index(name = "idx_attendance_branch_date", columnList = "branch_id, clock_in"),
                @Index(name = "idx_attendance_status", columnList = "tenant_id, status")
        }
)
public class AttendanceRecordPersistenceEntity extends HrAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
    private UUID tenantId;

    @Column(name = "branch_id", columnDefinition = "uuid", nullable = false)
    private UUID branchId;

    @Column(name = "membership_id", columnDefinition = "uuid", nullable = false)
    private UUID membershipId;

    @Column(name = "shift_id", columnDefinition = "uuid", nullable = false)
    private UUID shiftId;

    @Column(name = "clock_in", nullable = false)
    private Instant clockIn;

    @Column(name = "clock_out")
    private Instant clockOut;

    @Convert(converter = AttendanceStatusAttributeConverter.class)
    @Column(name = "status", length = 20, nullable = false)
    private AttendanceStatus status = AttendanceStatus.ON_TIME;

    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;

    @Column(name = "distance_to_branch_m", nullable = false)
    private Integer distanceToBranchMeters = 0;

    @Column(name = "justification_reason", length = 255)
    private String justificationReason;

    @Column(name = "justified_by", columnDefinition = "uuid")
    private UUID justifiedBy;

    @Column(name = "justified_at")
    private Instant justifiedAt;

    public AttendanceRecordPersistenceEntity(UUID id) {
        super(id);
    }
}
