package com.andeva.atelier.platform.billing.interfaces.rest.resources.responses;

/**
 * REST acknowledgment response returned to Stripe upon successful intake of an asynchronous webhook event.
 *
 * @author Joel Huamani Estefanero
 */
public record StripeWebhookAcknowledgmentResponse(
        boolean received,
        String eventId,
        String status
) {
}
