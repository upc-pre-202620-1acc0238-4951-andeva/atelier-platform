package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters.FaultSeverityConverter;
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
 * JPA persistence entity mapped to the {@code vehicle_faults} relational table.
 * Persists vehicle ECU diagnostic trouble codes (DTC) and their resolution lifecycle.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "vehicle_faults",
        indexes = {
                @Index(name = "idx_faults_vehicle_dtc", columnList = "vehicle_id, dtc_code"),
                @Index(name = "idx_faults_detected_at", columnList = "detected_at")
        }
)
public class VehicleFaultPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "dtc_code", nullable = false, length = 10)
    private String dtcCode;

    @Convert(converter = FaultSeverityConverter.class)
    @Column(name = "severity", nullable = false, length = 20)
    private FaultSeverity severity;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "is_resolved", nullable = false)
    private boolean isResolved;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public VehicleFaultPersistenceEntity(UUID id) {
        super(id);
    }
}
