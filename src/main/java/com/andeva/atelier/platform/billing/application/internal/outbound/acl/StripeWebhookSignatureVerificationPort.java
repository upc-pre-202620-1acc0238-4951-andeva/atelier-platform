package com.andeva.atelier.platform.billing.application.internal.outbound.acl;

/**
 * Outbound port for cryptographic Stripe Webhook signature validation.
 * Offloads HMAC-SHA256 signature verification and timestamp tolerance checks to the infrastructure layer.
 *
 * @author Joel Huamani Estefanero
 */
public interface StripeWebhookSignatureVerificationPort {

    long DEFAULT_TOLERANCE_SECONDS = 300L;

    /**
     * Verifies the cryptographic HMAC-SHA256 signature of an inbound Stripe webhook payload.
     *
     * @param payload          raw JSON payload string received in the request body
     * @param signatureHeader  value of the {@code Stripe-Signature} HTTP header (e.g. t=...,v1=...)
     * @param toleranceSeconds maximum allowable difference in seconds between event timestamp and current clock
     * @return true if the signature is valid and within the tolerance window, false otherwise
     */
    boolean verifySignature(String payload, String signatureHeader, long toleranceSeconds);

    /**
     * Verifies the signature or throws an invalid signature exception if verification fails
     * or timestamp is outside the replay tolerance window.
     *
     * @param payload          raw JSON payload string
     * @param signatureHeader  value of the {@code Stripe-Signature} HTTP header
     * @param toleranceSeconds maximum allowable difference in seconds
     */
    void verifyOrThrow(String payload, String signatureHeader, long toleranceSeconds);
}
