package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "work_shifts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_work_shifts_tenant_name", columnNames = {"tenant_id", "name"})
        },
        indexes = {
                @Index(name = "idx_work_shifts_tenant_name", columnList = "tenant_id, name"),
                @Index(name = "idx_work_shifts_active", columnList = "tenant_id, is_active")
        }
)
public class WorkShiftPersistenceEntity extends HrAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
    private UUID tenantId;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "grace_period_m", nullable = false)
    private Integer gracePeriodMinutes = 15;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public WorkShiftPersistenceEntity(UUID id) {
        super(id);
    }
}
