package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.StripeWebhookEventPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link StripeWebhookEventPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface StripeWebhookEventPersistenceRepository extends JpaRepository<StripeWebhookEventPersistenceEntity, UUID> {

    /**
     * Finds a webhook event record by Stripe Event ID.
     *
     * @param stripeEventId Stripe event identifier (e.g. evt_123)
     * @return optional containing the event entity
     */
    Optional<StripeWebhookEventPersistenceEntity> findByStripeEventId(String stripeEventId);

    /**
     * Checks if a Stripe event ID has already been recorded (idempotency shield).
     *
     * @param stripeEventId Stripe event identifier (e.g. evt_123)
     * @return true if recorded previously
     */
    boolean existsByStripeEventId(String stripeEventId);
}
