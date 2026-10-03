package com.andeva.atelier.platform.billing.domain;

import com.andeva.atelier.platform.billing.domain.model.aggregates.StripeWebhookEvent;
import com.andeva.atelier.platform.billing.domain.model.enums.WebhookProcessingStatus;
import com.andeva.atelier.platform.billing.domain.model.events.StripeWebhookProcessedEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.StripeEventId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests covering StripeWebhookEvent aggregate root, idempotency states,
 * and lifecycle transitions (receive, markProcessed, markFailed, markIgnored).
 *
 * @author Joel Huamani Estefanero
 */
class StripeWebhookEventAggregateTest {

    @Test
    @DisplayName("Should receive webhook event and transition to PROCESSED with event emission")
    void shouldHandleWebhookLifecycle() {
        StripeEventId eventId = StripeEventId.of("evt_test123456");
        String payload = "{\"id\":\"evt_test123456\",\"type\":\"invoice.payment_succeeded\"}";

        StripeWebhookEvent event = StripeWebhookEvent.receive(eventId, "invoice.payment_succeeded", payload);

        assertThat(event.id()).isNotNull();
        assertThat(event.getId()).isEqualTo(event.id());
        assertThat(event.stripeEventId()).isEqualTo(eventId);
        assertThat(event.getStripeEventId()).isEqualTo(eventId);
        assertThat(event.eventType()).isEqualTo("invoice.payment_succeeded");
        assertThat(event.getEventType()).isEqualTo("invoice.payment_succeeded");
        assertThat(event.eventPayload()).isEqualTo(payload);
        assertThat(event.getEventPayload()).isEqualTo(payload);
        assertThat(event.status()).isEqualTo(WebhookProcessingStatus.PENDING);
        assertThat(event.getStatus()).isEqualTo(WebhookProcessingStatus.PENDING);
        assertThat(event.processedAt()).isEmpty();
        assertThat(event.getProcessedAt()).isNull();
        assertThat(event.errorMessage()).isEmpty();
        assertThat(event.getErrorMessage()).isNull();

        Instant processedTime = Instant.now();
        event.markProcessed(processedTime);

        assertThat(event.status()).isEqualTo(WebhookProcessingStatus.PROCESSED);
        assertThat(event.processedAt()).contains(processedTime);
        assertThat(event.getProcessedAt()).isEqualTo(processedTime);

        assertThat(event.domainEvents()).hasSize(1);
        Object domainEvent = event.domainEvents().iterator().next();
        assertThat(domainEvent).isInstanceOf(StripeWebhookProcessedEvent.class);
        StripeWebhookProcessedEvent webhookProcessedEvent = (StripeWebhookProcessedEvent) domainEvent;
        assertThat(webhookProcessedEvent.eventId()).isEqualTo(eventId);
        assertThat(webhookProcessedEvent.eventType()).isEqualTo("invoice.payment_succeeded");
        assertThat(webhookProcessedEvent.processedAt()).isEqualTo(processedTime);
    }

    @Test
    @DisplayName("Should mark processed with default timestamp")
    void shouldMarkProcessedWithDefaultTimestamp() {
        StripeEventId eventId = StripeEventId.of("evt_default_now");
        StripeWebhookEvent event = StripeWebhookEvent.receive(eventId, "customer.subscription.updated", "{}");

        event.markProcessed();

        assertThat(event.status()).isEqualTo(WebhookProcessingStatus.PROCESSED);
        assertThat(event.processedAt()).isPresent();
        assertThat(event.domainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should mark webhook event as FAILED with error message")
    void shouldHandleFailure() {
        StripeWebhookEvent failedEvent = StripeWebhookEvent.receive(
                StripeEventId.of("evt_fail123"),
                "invoice.payment_failed",
                "{}"
        );
        failedEvent.markFailed("Database deadlock during checkout");

        assertThat(failedEvent.status()).isEqualTo(WebhookProcessingStatus.FAILED);
        assertThat(failedEvent.getStatus()).isEqualTo(WebhookProcessingStatus.FAILED);
        assertThat(failedEvent.errorMessage()).contains("Database deadlock during checkout");
        assertThat(failedEvent.getErrorMessage()).isEqualTo("Database deadlock during checkout");
        assertThat(failedEvent.processedAt()).isPresent();
    }

    @Test
    @DisplayName("Should mark webhook event as IGNORED")
    void shouldHandleIgnored() {
        StripeWebhookEvent ignoredEvent = StripeWebhookEvent.receive(
                StripeEventId.of("evt_ignore123"),
                "customer.created",
                "{}"
        );
        ignoredEvent.markIgnored();

        assertThat(ignoredEvent.status()).isEqualTo(WebhookProcessingStatus.IGNORED);
        assertThat(ignoredEvent.getStatus()).isEqualTo(WebhookProcessingStatus.IGNORED);
        assertThat(ignoredEvent.processedAt()).isPresent();
        assertThat(ignoredEvent.errorMessage()).isEmpty();
    }

    @Test
    @DisplayName("Should validate required fields in constructor invariants")
    void shouldValidateConstructorInvariants() {
        UUID id = UUID.randomUUID();
        StripeEventId eventId = StripeEventId.of("evt_valid123");

        assertThatThrownBy(() -> new StripeWebhookEvent(null, eventId, "type", "{}", WebhookProcessingStatus.PENDING, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Internal UUID id");

        assertThatThrownBy(() -> new StripeWebhookEvent(id, null, "type", "{}", WebhookProcessingStatus.PENDING, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("StripeEventId");

        assertThatThrownBy(() -> new StripeWebhookEvent(id, eventId, null, "{}", WebhookProcessingStatus.PENDING, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("eventType");

        assertThatThrownBy(() -> new StripeWebhookEvent(id, eventId, "type", null, WebhookProcessingStatus.PENDING, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("eventPayload");

        assertThatThrownBy(() -> new StripeWebhookEvent(id, eventId, "type", "{}", null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("WebhookProcessingStatus");
    }
}
