package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.billing.domain.model.aggregates.StripeWebhookEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.StripeEventId;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.StripeWebhookEventPersistenceEntity;
import org.springframework.stereotype.Component;

/**
 * Assembler transforming between {@link StripeWebhookEvent} Domain Aggregate Root
 * and {@link StripeWebhookEventPersistenceEntity} JPA entity.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class StripeWebhookEventPersistenceAssembler {

    /**
     * Converts a domain aggregate into a JPA persistence entity.
     *
     * @param domain domain aggregate root
     * @return JPA persistence entity
     */
    public StripeWebhookEventPersistenceEntity toEntity(StripeWebhookEvent domain) {
        if (domain == null) {
            return null;
        }

        return new StripeWebhookEventPersistenceEntity(
                domain.id(),
                domain.stripeEventId().value(),
                domain.eventType(),
                domain.eventPayload(),
                domain.status(),
                domain.processedAt().orElse(null),
                domain.errorMessage().orElse(null)
        );
    }

    /**
     * Reconstitutes a domain aggregate from a JPA persistence entity.
     *
     * @param entity JPA persistence entity
     * @return domain aggregate root
     */
    public StripeWebhookEvent toDomain(StripeWebhookEventPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new StripeWebhookEvent(
                entity.getId(),
                new StripeEventId(entity.getStripeEventId()),
                entity.getType(),
                entity.getPayload(),
                entity.getStatus(),
                entity.getProcessedAt(),
                entity.getErrorMessage()
        );
    }
}
