package com.andeva.atelier.platform.billing.infrastructure.external.payment.stripe;

import com.andeva.atelier.platform.billing.application.internal.outbound.acl.StripeWebhookSignatureVerificationPort;
import com.andeva.atelier.platform.billing.domain.exceptions.InvalidWebhookSignatureException;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Infrastructure outbound adapter implementing {@link StripeWebhookSignatureVerificationPort}
 * using official {@code stripe-java} SDK cryptographic verification.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class StripeWebhookSignatureVerifierAdapter implements StripeWebhookSignatureVerificationPort {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookSignatureVerifierAdapter.class);

    private final String webhookSecret;

    public StripeWebhookSignatureVerifierAdapter(
            @Value("${stripe.webhook-secret:whsec_default_secret}") String webhookSecret
    ) {
        this.webhookSecret = webhookSecret != null ? webhookSecret : "";
    }

    @Override
    public boolean verifySignature(String payload, String signatureHeader, long toleranceSeconds) {
        try {
            return Webhook.Signature.verifyHeader(payload, signatureHeader, this.webhookSecret, toleranceSeconds);
        } catch (Exception ex) {
            log.warn("Stripe official SDK signature verification failed: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public void verifyOrThrow(String payload, String signatureHeader, long toleranceSeconds) {
        try {
            boolean valid = Webhook.Signature.verifyHeader(payload, signatureHeader, this.webhookSecret, toleranceSeconds);
            if (!valid) {
                throw new InvalidWebhookSignatureException("Invalid Stripe signature: verification returned false");
            }
        } catch (InvalidWebhookSignatureException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Stripe official SDK signature verification failed: {}", ex.getMessage());
            throw new InvalidWebhookSignatureException("Invalid Stripe signature: " + ex.getMessage(), ex);
        }
    }
}
