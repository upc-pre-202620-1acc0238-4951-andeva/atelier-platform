package com.andeva.atelier.platform.billing.domain.exceptions;

/**
 * Thrown when an unrecoverable communications or API error occurs with the external Stripe gateway.
 *
 * @author Joel Huamani Estefanero
 */
public class StripeIntegrationException extends BillingDomainException {

    public StripeIntegrationException(String message) {
        super("STRIPE_INTEGRATION", message);
    }

    public StripeIntegrationException(String message, Throwable cause) {
        super("STRIPE_INTEGRATION", message + (cause != null ? ": " + cause.getMessage() : ""));
        if (cause != null) {
            initCause(cause);
        }
    }
}
