package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.billing.domain.model.enums.WebhookProcessingStatus;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters.WebhookProcessingStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code stripe_events} relational table.
 * Acts as an atomic database idempotency shield against duplicate Stripe webhook deliveries
 * and provides full audit logging for asynchronous event processing.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "stripe_webhook_events",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stripe_webhook_events_id", columnNames = {"stripe_event_id"})
        },
        indexes = {
                @Index(name = "idx_stripe_webhook_events_status", columnList = "status"),
                @Index(name = "idx_stripe_webhook_events_type_status", columnList = "type, status, processed_at")
        }
)
public class StripeWebhookEventPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "stripe_event_id", nullable = false, length = 100, unique = true)
    private String stripeEventId;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Convert(converter = WebhookProcessingStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private WebhookProcessingStatus status;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public StripeWebhookEventPersistenceEntity(
            UUID id,
            String stripeEventId,
            String type,
            String payload,
            WebhookProcessingStatus status,
            Instant processedAt,
            String errorMessage
    ) {
        this.id = id;
        this.stripeEventId = stripeEventId;
        this.type = type;
        this.payload = payload;
        this.status = status;
        this.processedAt = processedAt;
        this.errorMessage = errorMessage;
    }
}
