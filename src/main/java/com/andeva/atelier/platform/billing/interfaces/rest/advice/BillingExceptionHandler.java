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
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Controller advice translating SaaS Billing domain exceptions into standardized
 * RFC 7807 ProblemDetail responses with dynamic internationalization (i18n).
 *
 * @author Joel Huamani Estefanero
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.andeva.atelier.platform.billing.interfaces.rest")
public class BillingExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BillingExceptionHandler.class);
    private static final String BASE_TYPE_URL = "https://api.atelier.andeva.com/errors/";
    private static final String MESSAGES_BASENAME = "messages";

    private final MessageSource messageSource;

    public BillingExceptionHandler() {
        this(null);
    }

    @Autowired
    public BillingExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(PlanNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePlanNotFound(PlanNotFoundException ex, HttpServletRequest request) {
        log.warn("Commercial subscription plan not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "plan-not-found", "Plan Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSubscriptionNotFound(SubscriptionNotFoundException ex, HttpServletRequest request) {
        log.warn("Tenant subscription not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "subscription-not-found", "Subscription Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(SaasInvoiceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSaasInvoiceNotFound(SaasInvoiceNotFoundException ex, HttpServletRequest request) {
        log.warn("SaaS invoice not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "saas-invoice-not-found", "SaaS Invoice Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ProblemDetail> handleQuotaExceeded(QuotaExceededException ex, HttpServletRequest request) {
        log.warn("Subscription quota limit exceeded: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "quota-exceeded", "Quota Exceeded", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(DuplicateActiveSubscriptionException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateSubscription(DuplicateActiveSubscriptionException ex, HttpServletRequest request) {
        log.warn("Duplicate active subscription conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "duplicate-subscription", "Duplicate Subscription", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(SubscriptionPastDueException.class)
    public ResponseEntity<ProblemDetail> handleSubscriptionPastDue(SubscriptionPastDueException ex, HttpServletRequest request) {
        log.warn("Subscription is past due: {}", ex.getMessage());
        return buildResponse(HttpStatus.PAYMENT_REQUIRED, "subscription-past-due", "Subscription Past Due", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(InvalidWebhookSignatureException.class)
    public ResponseEntity<ProblemDetail> handleInvalidWebhookSignature(InvalidWebhookSignatureException ex, HttpServletRequest request) {
        log.warn("Invalid webhook signature: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "invalid-webhook-signature", "Invalid Webhook Signature", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(InvalidPlanPricingException.class)
    public ResponseEntity<ProblemDetail> handleInvalidPlanPricing(InvalidPlanPricingException ex, HttpServletRequest request) {
        log.warn("Invalid plan pricing: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "invalid-plan-pricing", "Invalid Plan Pricing", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(StripeIntegrationException.class)
    public ResponseEntity<ProblemDetail> handleStripeIntegration(StripeIntegrationException ex, HttpServletRequest request) {
        log.error("Stripe gateway error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_GATEWAY, "stripe-integration-error", "Stripe Integration Error", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(StripeWebhookProcessingException.class)
    public ResponseEntity<ProblemDetail> handleStripeWebhookProcessing(StripeWebhookProcessingException ex, HttpServletRequest request) {
        log.error("Stripe webhook processing error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "stripe-webhook-processing-failed", "Webhook Processing Failed", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(BillingDomainException.class)
    public ResponseEntity<ProblemDetail> handleBillingDomain(BillingDomainException ex, HttpServletRequest request) {
        log.warn("Billing domain error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "billing-error", "Billing Error", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied in billing: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "access-denied", "Access Denied", ex.getMessage(), "ACCESS_DENIED", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument in billing request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "invalid-argument", "Invalid Argument", ex.getMessage(), "INVALID_ARGUMENT", request);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(org.springframework.web.bind.MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failure in billing REST request: {} errors", ex.getBindingResult().getErrorCount());
        String detailMessage = resolveMessageOrDefault("VALIDATION_FAILED", "Validation failed for one or more fields");
        String title = resolveTitleOrDefault("validation-failed", "Validation Failed");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detailMessage);
        problem.setType(URI.create(BASE_TYPE_URL + "validation-failed"));
        problem.setTitle(title);
        problem.setProperty("errorCode", "VALIDATION_FAILED");
        problem.setProperty("code", "VALIDATION_FAILED");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("invalidFields", ex.getBindingResult().getFieldErrors().stream()
                .map(f -> java.util.Map.of("field", f.getField(), "message", f.getDefaultMessage() != null ? f.getDefaultMessage() : ""))
                .toList());
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request body: {}", ex.getMessage());
        String defaultMsg = "Malformed or unreadable JSON payload";
        String resolvedMsg = resolveMessageOrDefault("MALFORMED_REQUEST", defaultMsg);
        return buildResponse(HttpStatus.BAD_REQUEST, "malformed-request", "Malformed Request", resolvedMsg, "MALFORMED_REQUEST", request);
    }

    private ResponseEntity<ProblemDetail> buildResponse(
            HttpStatus status,
            String typeSuffix,
            String defaultTitle,
            String rawMessage,
            String errorCode,
            HttpServletRequest request
    ) {
        String detailMessage = resolveMessageOrDefault(errorCode, rawMessage);
        String title = resolveTitleOrDefault(typeSuffix, defaultTitle);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detailMessage);
        problem.setType(URI.create(BASE_TYPE_URL + typeSuffix));
        problem.setTitle(title);
        problem.setProperty("errorCode", errorCode != null ? errorCode : "BILLING_ERROR");
        problem.setProperty("code", errorCode != null ? errorCode : "BILLING_ERROR");
        problem.setProperty("timestamp", Instant.now());
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }

        return ResponseEntity.status(status).body(problem);
    }

    private String resolveTitleOrDefault(String typeSuffix, String defaultTitle) {
        if (typeSuffix == null) return defaultTitle;
        String key = "title." + typeSuffix.replace('-', '_');
        return resolveMessageOrDefault(key, defaultTitle);
    }

    private String resolveMessageOrDefault(String errorCode, String defaultMessage, Object... args) {
        if (errorCode == null || errorCode.isBlank()) {
            return defaultMessage;
        }

        Locale currentLocale = LocaleContextHolder.getLocale();
        String codeLower = errorCode.toLowerCase(Locale.ROOT);
        List<String> candidates = List.of(
                errorCode,
                "error.domain." + codeLower,
                "error.application." + codeLower,
                "error." + codeLower.replace('_', '.')
        );

        if (messageSource != null) {
            for (String key : candidates) {
                try {
                    return messageSource.getMessage(key, args, currentLocale);
                } catch (Exception ignored) {
                }
            }
        }

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, currentLocale);
            for (String key : candidates) {
                if (bundle.containsKey(key)) {
                    return MessageFormat.format(bundle.getString(key), args);
                }
            }
        } catch (MissingResourceException ignored) {
            // Fallback to default message
        }

        return defaultMessage;
    }
}
