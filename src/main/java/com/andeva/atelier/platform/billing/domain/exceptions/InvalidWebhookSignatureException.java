package com.andeva.atelier.platform.billing.domain.exceptions;

/**
 * Thrown when an incoming Stripe webhook payload signature header is missing, malformed, or fails HMAC-SHA256 verification.
 *
 * @author Joel Huamani Estefanero
 */
public class InvalidWebhookSignatureException extends BillingDomainException {

    public InvalidWebhookSignatureException(String message) {
        super("INVALID_WEBHOOK_SIGNATURE", message);
    }

    public InvalidWebhookSignatureException(String message, Throwable cause) {
        super("INVALID_WEBHOOK_SIGNATURE", message + (cause != null ? ": " + cause.getMessage() : ""));
        if (cause != null) {
            initCause(cause);
        }
    }
}
