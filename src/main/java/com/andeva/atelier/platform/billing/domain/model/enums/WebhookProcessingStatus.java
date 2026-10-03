package com.andeva.atelier.platform.billing.domain.model.enums;

/**
 * Processing and idempotency status for incoming Stripe webhook events.
 *
 * @author Joel Huamani Estefanero
 */
public enum WebhookProcessingStatus {
    PENDING,
    PROCESSED,
    FAILED,
    IGNORED
}
