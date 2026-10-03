package com.andeva.atelier.platform.billing.domain.model.events;

import com.andeva.atelier.platform.billing.domain.model.ids.StripeEventId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when an incoming Stripe webhook notification is idempotently resolved and processed.
 *
 * @author Joel Huamani Estefanero
 */
public record StripeWebhookProcessedEvent(
        StripeEventId eventId,
        String eventType,
        Instant processedAt,
        Instant occurredOn
) implements Serializable {

    public StripeWebhookProcessedEvent {
        Objects.requireNonNull(eventId, "StripeEventId cannot be null");
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(processedAt, "processedAt cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static StripeWebhookProcessedEvent of(StripeEventId eventId, String eventType, Instant processedAt) {
        return new StripeWebhookProcessedEvent(eventId, eventType, processedAt, Instant.now());
    }
}
