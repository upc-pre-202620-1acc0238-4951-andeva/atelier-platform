package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for issued AI vehicle health reports; the full structured diagnosis is
 * stored as JSONB next to the indexed summary columns.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "vehicle_health_reports",
        indexes = {
                @Index(name = "idx_health_reports_vehicle_generated", columnList = "tenant_id, vehicle_id, generated_at"),
                @Index(name = "idx_health_reports_tenant_generated", columnList = "tenant_id, generated_at")
        }
)
public class VehicleHealthReportPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "vehicle_id", nullable = false, updatable = false)
    private UUID vehicleId;

    @Column(name = "days_analyzed", nullable = false, updatable = false)
    private int daysAnalyzed;

    @Column(name = "health_score", nullable = false, updatable = false)
    private int healthScore;

    @Column(name = "overall_condition", nullable = false, updatable = false, length = 30)
    private String overallCondition;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "diagnosis", nullable = false, updatable = false, columnDefinition = "jsonb")
    private String diagnosis;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt;

    public VehicleHealthReportPersistenceEntity(UUID id) {
        super(id);
    }
}
