package com.andeva.atelier.platform.billing.interfaces.rest.advice;

import com.andeva.atelier.platform.billing.domain.exceptions.BillingDomainException;
import com.andeva.atelier.platform.billing.domain.exceptions.DuplicateActiveSubscriptionException;
import com.andeva.atelier.platform.billing.domain.exceptions.InvalidPlanPricingException;
import com.andeva.atelier.platform.billing.domain.exceptions.InvalidWebhookSignatureException;
import com.andeva.atelier.platform.billing.domain.exceptions.PlanNotFoundException;
import com.andeva.atelier.platform.billing.domain.exceptions.QuotaExceededException;
import com.andeva.atelier.platform.billing.domain.exceptions.SaasInvoiceNotFoundException;
import com.andeva.atelier.platform.billing.domain.exceptions.StripeIntegrationException;
import com.andeva.atelier.platform.billing.domain.exceptions.StripeWebhookProcessingException;
import com.andeva.atelier.platform.billing.domain.exceptions.SubscriptionNotFoundException;
import com.andeva.atelier.platform.billing.domain.exceptions.SubscriptionPastDueException;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for {@link BillingExceptionHandler}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("BillingExceptionHandler Unit Tests")
class BillingExceptionHandlerTest {

    private BillingExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");

        handler = new BillingExceptionHandler(messageSource);
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/billing/test");
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    @DisplayName("PlanNotFoundException maps to 404 NOT_FOUND")
    void handlePlanNotFound() {
        PlanNotFoundException ex = new PlanNotFoundException(PlanId.generate());
        ResponseEntity<ProblemDetail> response = handler.handlePlanNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getType().toString()).contains("plan-not-found");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("PLAN_NOT_FOUND");
    }

    @Test
    @DisplayName("SubscriptionNotFoundException maps to 404 NOT_FOUND")
    void handleSubscriptionNotFound() {
        SubscriptionNotFoundException ex = new SubscriptionNotFoundException(SubscriptionId.generate());
        ResponseEntity<ProblemDetail> response = handler.handleSubscriptionNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getType().toString()).contains("subscription-not-found");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("SUBSCRIPTION_NOT_FOUND");
    }

    @Test
    @DisplayName("SaasInvoiceNotFoundException maps to 404 NOT_FOUND")
    void handleSaasInvoiceNotFound() {
        SaasInvoiceNotFoundException ex = new SaasInvoiceNotFoundException(SaasInvoiceId.generate());
        ResponseEntity<ProblemDetail> response = handler.handleSaasInvoiceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getType().toString()).contains("saas-invoice-not-found");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("SAAS_INVOICE_NOT_FOUND");
    }

    @Test
    @DisplayName("QuotaExceededException maps to 403 FORBIDDEN")
    void handleQuotaExceeded() {
        QuotaExceededException ex = new QuotaExceededException("Branches quota exceeded");
        ResponseEntity<ProblemDetail> response = handler.handleQuotaExceeded(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getType().toString()).contains("quota-exceeded");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("QUOTA_EXCEEDED");
    }

    @Test
    @DisplayName("DuplicateActiveSubscriptionException maps to 409 CONFLICT")
    void handleDuplicateSubscription() {
        DuplicateActiveSubscriptionException ex = new DuplicateActiveSubscriptionException(TenantId.generate());
        ResponseEntity<ProblemDetail> response = handler.handleDuplicateSubscription(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getType().toString()).contains("duplicate-subscription");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("DUPLICATE_ACTIVE_SUBSCRIPTION");
    }

    @Test
    @DisplayName("SubscriptionPastDueException maps to 402 PAYMENT_REQUIRED")
    void handleSubscriptionPastDue() {
        SubscriptionPastDueException ex = new SubscriptionPastDueException(TenantId.generate());
        ResponseEntity<ProblemDetail> response = handler.handleSubscriptionPastDue(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(402);
        assertThat(response.getBody().getType().toString()).contains("subscription-past-due");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("SUBSCRIPTION_PAST_DUE");
    }

    @Test
    @DisplayName("InvalidWebhookSignatureException maps to 401 UNAUTHORIZED")
    void handleInvalidWebhookSignature() {
        InvalidWebhookSignatureException ex = new InvalidWebhookSignatureException("HMAC failure");
        ResponseEntity<ProblemDetail> response = handler.handleInvalidWebhookSignature(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getType().toString()).contains("invalid-webhook-signature");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("INVALID_WEBHOOK_SIGNATURE");
    }

    @Test
    @DisplayName("InvalidPlanPricingException maps to 400 BAD_REQUEST")
    void handleInvalidPlanPricing() {
        InvalidPlanPricingException ex = new InvalidPlanPricingException("Price must be positive");
        ResponseEntity<ProblemDetail> response = handler.handleInvalidPlanPricing(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getType().toString()).contains("invalid-plan-pricing");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("INVALID_PLAN_PRICING");
    }

    @Test
    @DisplayName("StripeIntegrationException maps to 502 BAD_GATEWAY")
    void handleStripeIntegration() {
        StripeIntegrationException ex = new StripeIntegrationException("Stripe API down", null);
        ResponseEntity<ProblemDetail> response = handler.handleStripeIntegration(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(502);
        assertThat(response.getBody().getType().toString()).contains("stripe-integration-error");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("STRIPE_INTEGRATION");
    }

    @Test
    @DisplayName("StripeWebhookProcessingException maps to 422 UNPROCESSABLE_ENTITY")
    void handleStripeWebhookProcessing() {
        StripeWebhookProcessingException ex = new StripeWebhookProcessingException("Decoding failed", null);
        ResponseEntity<ProblemDetail> response = handler.handleStripeWebhookProcessing(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getType().toString()).contains("webhook-processing-failed");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("STRIPE_WEBHOOK_PROCESSING");
    }

    @Test
    @DisplayName("AccessDeniedException maps to 403 FORBIDDEN")
    void handleAccessDenied() {
        org.springframework.security.access.AccessDeniedException ex = new org.springframework.security.access.AccessDeniedException("Cross-tenant breach");
        ResponseEntity<ProblemDetail> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getType().toString()).contains("access-denied");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("Generic BillingDomainException maps to 400 BAD_REQUEST")
    void handleGenericBillingDomainException() {
        BillingDomainException ex = new BillingDomainException("CUSTOM_CODE", "Custom invariant breach") {};
        ResponseEntity<ProblemDetail> response = handler.handleBillingDomain(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getType().toString()).contains("billing-error");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("CUSTOM_CODE");
    }

    @Test
    @DisplayName("i18n message lookup adapts to Spanish locale with exact localized text")
    void i18nMessageLookupSpanish() {
        LocaleContextHolder.setLocale(Locale.of("es"));
        QuotaExceededException ex = new QuotaExceededException("Branches quota exceeded");
        ResponseEntity<ProblemDetail> response = handler.handleQuotaExceeded(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Límite de cuota excedido");
        assertThat(response.getBody().getDetail()).isEqualTo("Se ha superado el limite maximo de cuota operativa de su plan. Por favor aumente de categoria comercial.");
    }

    @Test
    @DisplayName("i18n message lookup adapts to English locale with exact localized text")
    void i18nMessageLookupEnglish() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        QuotaExceededException ex = new QuotaExceededException("Branches quota exceeded");
        ResponseEntity<ProblemDetail> response = handler.handleQuotaExceeded(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Quota Limit Exceeded");
        assertThat(response.getBody().getDetail()).isEqualTo("Operational quota ceiling exceeded for the active commercial tier. Please upgrade your plan.");
    }

    @Test
    @DisplayName("IllegalArgumentException maps to 400 BAD_REQUEST")
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Tenant context missing");
        ResponseEntity<ProblemDetail> response = handler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getType().toString()).contains("invalid-argument");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("INVALID_ARGUMENT");
        assertThat(response.getBody().getProperties().get("code")).isEqualTo("INVALID_ARGUMENT");
    }

    @Test
    @DisplayName("HttpMessageNotReadableException maps to 400 BAD_REQUEST")
    void handleHttpMessageNotReadable() {
        org.springframework.http.converter.HttpMessageNotReadableException ex =
                new org.springframework.http.converter.HttpMessageNotReadableException("Malformed JSON", (org.springframework.http.HttpInputMessage) null);
        ResponseEntity<ProblemDetail> response = handler.handleHttpMessageNotReadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getType().toString()).contains("malformed-request");
        assertThat(response.getBody().getProperties().get("errorCode")).isEqualTo("MALFORMED_REQUEST");
    }
}
