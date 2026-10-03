package com.andeva.atelier.platform.billing.domain.repositories;

import com.andeva.atelier.platform.billing.domain.model.aggregates.StripeWebhookEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.StripeEventId;

import java.util.Optional;

/**
 * Domain repository contract for managing Stripe webhook events and enforcing Exactly-Once idempotency.
 *
 * @author Joel Huamani Estefanero
 */
public interface StripeWebhookEventRepository {

    /**
     * Saves or updates a Stripe webhook event in persistence.
     *
     * @param event StripeWebhookEvent aggregate to persist
     * @return persisted StripeWebhookEvent instance
     */
    StripeWebhookEvent save(StripeWebhookEvent event);

    /**
     * Finds a webhook event by its unique Stripe event identifier (evt_...).
     *
     * @param stripeEventId StripeEventId
     * @return Optional containing the StripeWebhookEvent if found
     */
    Optional<StripeWebhookEvent> findByStripeEventId(StripeEventId stripeEventId);

    /**
     * Determines whether a Stripe webhook event has already been registered in the database.
     *
     * @param stripeEventId StripeEventId
     * @return true if the event has already been recorded
     */
    boolean existsByStripeEventId(StripeEventId stripeEventId);
}
