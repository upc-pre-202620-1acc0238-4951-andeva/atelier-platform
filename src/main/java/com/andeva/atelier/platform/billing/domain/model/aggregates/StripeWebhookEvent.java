package com.andeva.atelier.platform.billing.domain.model.aggregates;

import com.andeva.atelier.platform.billing.domain.model.enums.WebhookProcessingStatus;
import com.andeva.atelier.platform.billing.domain.model.events.StripeWebhookProcessedEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.StripeEventId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root ensuring Exactly-Once Processing and idempotency auditing of external Stripe Webhook events.
 *
 * @author Joel Huamani Estefanero
 */
public class StripeWebhookEvent extends AbstractDomainAggregateRoot<StripeWebhookEvent> {

    private final UUID id;
    private final StripeEventId stripeEventId;
    private final String eventType;
    private final String eventPayload;
    private WebhookProcessingStatus status;
    private Instant processedAt;
    private String errorMessage;

    public StripeWebhookEvent(
            UUID id,
            StripeEventId stripeEventId,
            String eventType,
            String eventPayload,
            WebhookProcessingStatus status,
            Instant processedAt,
            String errorMessage
    ) {
        this.id = Objects.requireNonNull(id, "Internal UUID id cannot be null");
        this.stripeEventId = Objects.requireNonNull(stripeEventId, "StripeEventId cannot be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType cannot be null");
        this.eventPayload = Objects.requireNonNull(eventPayload, "eventPayload cannot be null");
        this.status = Objects.requireNonNull(status, "WebhookProcessingStatus cannot be null");
        this.processedAt = processedAt;
        this.errorMessage = errorMessage;
    }

    public static StripeWebhookEvent receive(StripeEventId eventId, String type, String payload) {
        return new StripeWebhookEvent(
                UUID.randomUUID(),
                eventId,
                type,
                payload,
                WebhookProcessingStatus.PENDING,
                null,
                null
        );
    }

    public void markProcessed(Instant timestamp) {
        this.status = WebhookProcessingStatus.PROCESSED;
        this.processedAt = timestamp != null ? timestamp : Instant.now();
        registerDomainEvent(StripeWebhookProcessedEvent.of(this.stripeEventId, this.eventType, this.processedAt));
    }

    public void markProcessed() {
        markProcessed(Instant.now());
    }

    public void markFailed(String error) {
        this.status = WebhookProcessingStatus.FAILED;
        this.errorMessage = error;
        this.processedAt = Instant.now();
    }

    public void markIgnored() {
        this.status = WebhookProcessingStatus.IGNORED;
        this.processedAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public UUID getId() {
        return id;
    }

    public StripeEventId stripeEventId() {
        return stripeEventId;
    }

    public StripeEventId getStripeEventId() {
        return stripeEventId;
    }

    public String eventType() {
        return eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public String eventPayload() {
        return eventPayload;
    }

    public String getEventPayload() {
        return eventPayload;
    }

    public WebhookProcessingStatus status() {
        return status;
    }

    public WebhookProcessingStatus getStatus() {
        return status;
    }

    public Optional<Instant> processedAt() {
        return Optional.ofNullable(processedAt);
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public Optional<String> errorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
