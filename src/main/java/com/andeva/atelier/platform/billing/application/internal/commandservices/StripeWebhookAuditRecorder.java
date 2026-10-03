package com.andeva.atelier.platform.billing.application.internal.commandservices;

import com.andeva.atelier.platform.billing.domain.model.aggregates.StripeWebhookEvent;
import com.andeva.atelier.platform.billing.domain.repositories.StripeWebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Dedicated helper component ensuring webhook audit events and processing failures
 * are recorded in an isolated transaction boundary, preventing rollbacks from erasing audit logs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class StripeWebhookAuditRecorder {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookAuditRecorder.class);

    private final StripeWebhookEventRepository webhookEventRepository;

    public StripeWebhookAuditRecorder(StripeWebhookEventRepository webhookEventRepository) {
        this.webhookEventRepository = Objects.requireNonNull(webhookEventRepository, "StripeWebhookEventRepository cannot be null");
    }

    /**
     * Persists an audit failure event in an isolated new transaction boundary.
     *
     * @param webhookEvent the webhook event aggregate
     * @param errorMessage diagnostic error description
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(StripeWebhookEvent webhookEvent, String errorMessage) {
        if (webhookEvent == null) {
            return;
        }
        try {
            webhookEvent.markFailed(errorMessage);
            webhookEventRepository.save(webhookEvent);
            log.info("Persisted FAILED status in REQUIRES_NEW transaction for Stripe event: {}", webhookEvent.stripeEventId().value());
        } catch (Exception ex) {
            log.error("Failed to persist webhook failure audit log for event {}: {}", 
                    webhookEvent.stripeEventId().value(), ex.getMessage(), ex);
        }
    }
}
