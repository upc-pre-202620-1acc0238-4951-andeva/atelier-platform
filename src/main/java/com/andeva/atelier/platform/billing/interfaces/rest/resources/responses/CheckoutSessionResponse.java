package com.andeva.atelier.platform.billing.interfaces.rest.resources.responses;

/**
 * REST response providing the Stripe Checkout URL and session token for payment redirection.
 *
 * @author Joel Huamani Estefanero
 */
public record CheckoutSessionResponse(
        String checkoutUrl,
        String sessionId
) {
}
