package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters.AlertStatusConverter;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters.AlertTypeConverter;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code predictive_alerts} relational table.
 * Persists predictive maintenance warnings formulated by telemetry anomaly engines and AI inference.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "predictive_alerts",
        indexes = {
                @Index(name = "idx_alerts_vehicle", columnList = "vehicle_id"),
                @Index(name = "idx_alerts_status", columnList = "status")
        }
)
public class PredictiveAlertPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "recommended_service_id")
    private UUID recommendedServiceId;

    @Convert(converter = AlertTypeConverter.class)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Convert(converter = AlertStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private AlertStatus status;

    @Column(name = "fcm_message_id", length = 100)
    private String fcmMessageId;

    public PredictiveAlertPersistenceEntity(UUID id) {
        super(id);
    }
}
