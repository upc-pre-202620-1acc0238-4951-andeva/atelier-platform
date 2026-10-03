package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.crm.domain.model.enums.AppointmentStatus;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.converters.AppointmentStatusAttributeConverter;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code appointments} relational table.
 *
 * @author Adiel Sanchez Santin
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "appointments",
        indexes = {
                @Index(name = "idx_appt_tenant_branch_date", columnList = "tenant_id, branch_id, scheduled_at"),
                @Index(name = "idx_appt_customer", columnList = "customer_id"),
                @Index(name = "idx_appt_vehicle", columnList = "vehicle_id")
        }
)
public class AppointmentPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "estimated_duration_minutes", nullable = false)
    private int estimatedDurationMinutes;

    @Column(name = "reason", length = 2000)
    private String reason;

    @Convert(converter = AppointmentStatusAttributeConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    public AppointmentPersistenceEntity(UUID id) {
        super(id);
    }

    public AppointmentPersistenceEntity(
            UUID id,
            UUID tenantId,
            UUID branchId,
            UUID customerId,
            UUID vehicleId,
            Instant scheduledAt,
            int estimatedDurationMinutes,
            String reason,
            AppointmentStatus status,
            String cancellationReason
    ) {
        super(id);
        this.tenantId = tenantId;
        this.branchId = branchId;
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.scheduledAt = scheduledAt;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.reason = reason;
        this.status = status;
        this.cancellationReason = cancellationReason;
    }
}
