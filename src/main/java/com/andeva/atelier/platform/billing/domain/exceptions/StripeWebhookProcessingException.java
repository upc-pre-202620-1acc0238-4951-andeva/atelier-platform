package com.andeva.atelier.platform.billing.domain.exceptions;

/**
 * Thrown when an incoming Stripe webhook payload cannot be parsed or deserialized properly.
 *
 * @author Joel Huamani Estefanero
 */
public class StripeWebhookProcessingException extends BillingDomainException {

    public StripeWebhookProcessingException(String message) {
        super("STRIPE_WEBHOOK_PROCESSING", message);
    }

    public StripeWebhookProcessingException(String message, Throwable cause) {
        super("STRIPE_WEBHOOK_PROCESSING", message + (cause != null ? ": " + cause.getMessage() : ""));
        if (cause != null) {
            initCause(cause);
        }
    }
}
