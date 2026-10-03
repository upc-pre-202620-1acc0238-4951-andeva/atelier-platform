package com.andeva.atelier.platform.billing.application.commandservices;

import com.andeva.atelier.platform.billing.domain.model.commands.ProcessStripeWebhookCommand;

/**
 * Application Command Service contract for idempotent processing of asynchronous Stripe Webhook events.
 *
 * @author Joel Huamani Estefanero
 */
public interface StripeWebhookCommandService {

    /**
     * Processes an incoming Stripe webhook notification, authenticating its cryptographic signature
     * and enforcing Exactly-Once Processing semantics via an idempotency shield.
     *
     * @param command ProcessStripeWebhookCommand
     */
    void handle(ProcessStripeWebhookCommand command);
}
