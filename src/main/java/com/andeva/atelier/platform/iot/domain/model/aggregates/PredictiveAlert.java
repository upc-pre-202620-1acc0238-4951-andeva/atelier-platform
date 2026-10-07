package com.andeva.atelier.platform.iot.domain.model.aggregates;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.events.PredictiveAlertAcknowledgedEvent;
import com.andeva.atelier.platform.iot.domain.model.events.PredictiveAlertDispatchedEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root representing an analytical or AI-driven predictive maintenance alert,
 * warning drivers and workshop technicians before catastrophic mechanical failure.
 *
 * @author Joel Huamani Estefanero
 */
public class PredictiveAlert extends AbstractDomainAggregateRoot<PredictiveAlert> {

    private final AlertId id;
    private final VehicleId vehicleId;
    private final TenantId tenantId;
    private final ServiceId recommendedServiceId;
    private final AlertType alertType;
    private final ConfidenceScore confidenceScore;
    private final String message;
    private AlertStatus status;
    private String fcmMessageId;
    private final Instant createdAt;

    /**
     * Rehydration constructor for persistence assemblers.
     */
    public PredictiveAlert(
            AlertId id,
            VehicleId vehicleId,
            TenantId tenantId,
            Optional<ServiceId> recommendedServiceId,
            AlertType alertType,
            ConfidenceScore confidenceScore,
            String message,
            AlertStatus status,
            Optional<String> fcmMessageId,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "AlertId cannot be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.recommendedServiceId = recommendedServiceId != null ? recommendedServiceId.orElse(null) : null;
        this.alertType = Objects.requireNonNull(alertType, "AlertType cannot be null");
        this.confidenceScore = Objects.requireNonNull(confidenceScore, "ConfidenceScore cannot be null");
        this.message = Objects.requireNonNull(message, "Alert message cannot be null");
        this.status = Objects.requireNonNull(status, "AlertStatus cannot be null");
        this.fcmMessageId = fcmMessageId != null ? fcmMessageId.orElse(null) : null;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    /**
     * Domain factory method initializing a newly generated predictive maintenance alert.
     */
    public static PredictiveAlert create(
            VehicleId vehicleId,
            TenantId tenantId,
            Optional<ServiceId> recommendedServiceId,
            AlertType alertType,
            ConfidenceScore confidenceScore,
            String message
    ) {
        AlertId alertId = AlertId.generate();
        PredictiveAlert alert = new PredictiveAlert(
                alertId,
                vehicleId,
                tenantId,
                recommendedServiceId,
                alertType,
                confidenceScore,
                message,
                AlertStatus.DISPATCHED,
                Optional.empty(),
                Instant.now()
        );
        return alert;
    }

    public static PredictiveAlert create(
            VehicleId vehicleId,
            TenantId tenantId,
            AlertType alertType,
            ConfidenceScore confidenceScore,
            String message
    ) {
        return create(vehicleId, tenantId, Optional.empty(), alertType, confidenceScore, message);
    }

    /**
     * Records the dispatch of the push notification via Firebase Cloud Messaging.
     */
    public void recordDispatch(String fcmMessageId) {
        this.fcmMessageId = fcmMessageId;
        registerDomainEvent(PredictiveAlertDispatchedEvent.of(this.id, this.vehicleId, this.tenantId, fcmMessageId));
    }

    public void acknowledge() {
        if (this.status == AlertStatus.RESOLVED || this.status == AlertStatus.DISMISSED) {
            throw new IllegalStateException("Cannot acknowledge an alert that is already " + this.status);
        }
        this.status = AlertStatus.ACKNOWLEDGED;
        registerDomainEvent(PredictiveAlertAcknowledgedEvent.of(this.id, this.vehicleId));
    }

    public void resolve() {
        this.status = AlertStatus.RESOLVED;
    }

    public void dismiss() {
        this.status = AlertStatus.DISMISSED;
    }

    public AlertId getId() {
        return id;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public Optional<ServiceId> getRecommendedServiceId() {
        return Optional.ofNullable(recommendedServiceId);
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public ConfidenceScore getConfidenceScore() {
        return confidenceScore;
    }

    public String getMessage() {
        return message;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public Optional<String> getFcmMessageId() {
        return Optional.ofNullable(fcmMessageId);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
