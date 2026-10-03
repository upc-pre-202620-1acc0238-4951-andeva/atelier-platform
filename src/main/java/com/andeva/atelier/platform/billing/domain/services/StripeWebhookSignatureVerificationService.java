package com.andeva.atelier.platform.billing.domain.services;

import com.andeva.atelier.platform.billing.domain.exceptions.InvalidWebhookSignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Cryptographic Domain Service responsible for authenticating incoming Stripe Webhook HTTP notifications
 * by calculating and comparing HMAC-SHA256 signatures against configured webhook secrets.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class StripeWebhookSignatureVerificationService {

    private final String webhookSecret;

    public static final long DEFAULT_TOLERANCE_SECONDS = 300L;

    public StripeWebhookSignatureVerificationService() {
        this("whsec_default_secret");
    }

    public StripeWebhookSignatureVerificationService(@Value("${stripe.webhook.secret:whsec_default_secret}") String webhookSecret) {
        this.webhookSecret = webhookSecret != null ? webhookSecret : "";
    }

    /**
     * Verifies the cryptographic authenticity of a webhook request against the configured secret.
     * Throws an InvalidWebhookSignatureException if verification fails.
     *
     * @param payload   raw JSON body
     * @param sigHeader Stripe-Signature HTTP header value (e.g. "t=1234567,v1=abc...")
     */
    public void verifyOrThrow(String payload, String sigHeader) {
        verifyOrThrow(payload, sigHeader, 0L);
    }

    /**
     * Verifies the cryptographic authenticity and timestamp tolerance of a webhook request.
     *
     * @param payload          raw JSON body
     * @param sigHeader        Stripe-Signature HTTP header value
     * @param toleranceSeconds timestamp tolerance window in seconds (0 to disable tolerance check)
     */
    public void verifyOrThrow(String payload, String sigHeader, long toleranceSeconds) {
        if (toleranceSeconds > 0) {
            long timestamp = extractTimestamp(sigHeader);
            long now = java.time.Instant.now().getEpochSecond();
            if (Math.abs(now - timestamp) > toleranceSeconds) {
                throw new InvalidWebhookSignatureException("Stripe webhook timestamp is outside tolerance window (replay attack detected)");
            }
        }
        if (!verifySignature(payload, sigHeader, this.webhookSecret)) {
            throw new InvalidWebhookSignatureException("Cryptographic HMAC-SHA256 signature does not match configured webhook secret");
        }
    }

    /**
     * Computes the HMAC-SHA256 signature and returns whether it matches the received v1 signature.
     *
     * @param payload   raw JSON payload string
     * @param sigHeader Stripe-Signature header string
     * @param secret    webhook endpoint secret key
     * @return true if authentic, false otherwise
     */
    public boolean verifySignature(String payload, String sigHeader, String secret) {
        if (payload == null || sigHeader == null || sigHeader.isBlank() || secret == null || secret.isBlank()) {
            return false;
        }

        String[] elements = sigHeader.split(",");
        String timestamp = null;
        String signature = null;

        for (String element : elements) {
            String[] kv = element.trim().split("=", 2);
            if (kv.length == 2) {
                if ("t".equals(kv[0])) timestamp = kv[1];
                if ("v1".equals(kv[0])) signature = kv[1];
            }
        }

        if (timestamp == null || signature == null) {
            return false;
        }

        String signedPayload = timestamp + "." + payload;
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKey);
            byte[] hash = hmacSha256.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));

            StringBuilder computedSignature = new StringBuilder();
            for (byte b : hash) {
                computedSignature.append(String.format("%02x", b));
            }

            return MessageDigest.isEqual(
                    computedSignature.toString().getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extracts the timestamp component 't' from a Stripe-Signature header.
     *
     * @param sigHeader Stripe-Signature header string
     * @return UNIX timestamp as long
     */
    public long extractTimestamp(String sigHeader) {
        if (sigHeader == null || sigHeader.isBlank()) {
            throw new InvalidWebhookSignatureException("Missing Stripe-Signature header");
        }
        for (String element : sigHeader.split(",")) {
            String[] kv = element.trim().split("=", 2);
            if (kv.length == 2 && "t".equals(kv[0])) {
                try {
                    return Long.parseLong(kv[1]);
                } catch (NumberFormatException e) {
                    throw new InvalidWebhookSignatureException("Malformed timestamp in Stripe-Signature header");
                }
            }
        }
        throw new InvalidWebhookSignatureException("No timestamp 't' found in Stripe-Signature header");
    }
}
