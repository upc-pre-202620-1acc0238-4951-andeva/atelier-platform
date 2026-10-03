package com.andeva.atelier.platform.billing.domain;

import com.andeva.atelier.platform.billing.domain.exceptions.InvalidWebhookSignatureException;
import com.andeva.atelier.platform.billing.domain.services.StripeWebhookSignatureVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests covering StripeWebhookSignatureVerificationService cryptographic HMAC-SHA256 operations.
 * Validates authentic signature verification, tampered payload rejection, wrong secret rejection,
 * timestamp extraction, and boundary handling for malformed headers.
 *
 * @author Joel Huamani Estefanero
 */
class StripeWebhookSignatureVerificationServiceTest {

    private static final String SECRET = "whsec_test_secret_key_12345";
    private StripeWebhookSignatureVerificationService service;

    @BeforeEach
    void setUp() {
        service = new StripeWebhookSignatureVerificationService(SECRET);
    }

    private String computeHmacHex(String signedPayload, String secret) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = hmac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Should successfully verify authentic Stripe webhook signature")
    void shouldVerifyAuthenticSignature() {
        String payload = "{\"id\":\"evt_123\",\"type\":\"invoice.payment_succeeded\"}";
        long timestamp = 1717200000L;
        String signedPayload = timestamp + "." + payload;
        String v1Signature = computeHmacHex(signedPayload, SECRET);
        String sigHeader = "t=" + timestamp + ",v1=" + v1Signature;

        assertThatCode(() -> service.verifyOrThrow(payload, sigHeader))
                .doesNotThrowAnyException();
        assertThat(service.verifySignature(payload, sigHeader, SECRET)).isTrue();
    }

    @Test
    @DisplayName("Should verify signature when header contains additional unknown components")
    void shouldHandleAdditionalHeaderElements() {
        String payload = "{\"id\":\"evt_456\",\"type\":\"customer.subscription.updated\"}";
        long timestamp = 1717200050L;
        String signedPayload = timestamp + "." + payload;
        String v1Signature = computeHmacHex(signedPayload, SECRET);
        String sigHeader = "t=" + timestamp + ",v0=old_version_signature,v1=" + v1Signature;

        assertThatCode(() -> service.verifyOrThrow(payload, sigHeader))
                .doesNotThrowAnyException();
        assertThat(service.verifySignature(payload, sigHeader, SECRET)).isTrue();
    }

    @Test
    @DisplayName("Should reject tampered payload or incorrect secret")
    void shouldRejectTamperedOrWrongSecret() {
        String payload = "{\"id\":\"evt_123\",\"type\":\"invoice.payment_succeeded\"}";
        long timestamp = 1717200000L;
        String signedPayload = timestamp + "." + payload;
        String v1Signature = computeHmacHex(signedPayload, SECRET);
        String sigHeader = "t=" + timestamp + ",v1=" + v1Signature;

        // Tampered payload
        String tamperedPayload = "{\"id\":\"evt_123\",\"type\":\"invoice.payment_failed\"}";
        assertThatThrownBy(() -> service.verifyOrThrow(tamperedPayload, sigHeader))
                .isInstanceOf(InvalidWebhookSignatureException.class);
        assertThat(service.verifySignature(tamperedPayload, sigHeader, SECRET)).isFalse();

        // Wrong secret
        assertThat(service.verifySignature(payload, sigHeader, "whsec_wrong_secret")).isFalse();
    }

    @Test
    @DisplayName("Should reject null, empty, or incomplete signature parameters")
    void shouldRejectInvalidInputs() {
        String payload = "{\"id\":\"evt_123\"}";
        String validSig = "t=1717200000,v1=abcdef";

        // Null or blank payload
        assertThat(service.verifySignature(null, validSig, SECRET)).isFalse();

        // Null or blank sigHeader
        assertThat(service.verifySignature(payload, null, SECRET)).isFalse();
        assertThat(service.verifySignature(payload, "", SECRET)).isFalse();
        assertThat(service.verifySignature(payload, "   ", SECRET)).isFalse();

        // Null or blank secret
        assertThat(service.verifySignature(payload, validSig, null)).isFalse();
        assertThat(service.verifySignature(payload, validSig, "")).isFalse();
        assertThat(service.verifySignature(payload, validSig, "   ")).isFalse();

        // Missing timestamp 't'
        assertThat(service.verifySignature(payload, "v1=abcdef", SECRET)).isFalse();

        // Missing signature 'v1'
        assertThat(service.verifySignature(payload, "t=1717200000", SECRET)).isFalse();

        // verifyOrThrow with null secret in service instance
        StripeWebhookSignatureVerificationService nullSecretService = new StripeWebhookSignatureVerificationService(null);
        assertThatThrownBy(() -> nullSecretService.verifyOrThrow(payload, validSig))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

    @Test
    @DisplayName("Should extract timestamp accurately or throw on invalid header")
    void shouldExtractTimestamp() {
        String sigHeader = "t=1717200000,v1=abcdef123456";
        assertThat(service.extractTimestamp(sigHeader)).isEqualTo(1717200000L);

        // Header with multiple elements
        String multiHeader = "v0=abc,t=1717299999,v1=def";
        assertThat(service.extractTimestamp(multiHeader)).isEqualTo(1717299999L);

        // Null header
        assertThatThrownBy(() -> service.extractTimestamp(null))
                .isInstanceOf(InvalidWebhookSignatureException.class)
                .hasMessageContaining("Missing Stripe-Signature");

        // Blank header
        assertThatThrownBy(() -> service.extractTimestamp("   "))
                .isInstanceOf(InvalidWebhookSignatureException.class)
                .hasMessageContaining("Missing Stripe-Signature");

        // Missing 't'
        assertThatThrownBy(() -> service.extractTimestamp("v1=abcdef123456"))
                .isInstanceOf(InvalidWebhookSignatureException.class)
                .hasMessageContaining("No timestamp 't' found");

        // Malformed non-numeric timestamp
        assertThatThrownBy(() -> service.extractTimestamp("t=not_a_number,v1=abcdef"))
                .isInstanceOf(InvalidWebhookSignatureException.class)
                .hasMessageContaining("Malformed timestamp");
    }
}
