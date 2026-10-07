package com.andeva.atelier.platform.invoicing.interfaces.rest.advice;

import com.andeva.atelier.platform.invoicing.domain.exceptions.CorrelativeExhaustedException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.CreditNoteReferenceNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.CustomerFiscalDataMissingException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidTaxIdException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidVoucherAmountException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.SeriesNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.SunatIntegrationException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherAlreadyPaidException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherImmutableException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherNotFoundException;
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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
 * Controller advice translating Invoicing & Compliance domain exceptions
 * into standardized RFC 7807 ProblemDetail responses with dynamic internationalization (i18n).
 *
 * @author Joel Huamani Estefanero
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.andeva.atelier.platform.invoicing.interfaces.rest")
public class InvoicingExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(InvoicingExceptionHandler.class);
    private static final String BASE_TYPE_URL = "https://api.atelier.pe/errors/";
    private static final String MESSAGES_BASENAME = "messages";

    private final MessageSource messageSource;

    public InvoicingExceptionHandler() {
        this(null);
    }

    @Autowired
    public InvoicingExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(InvalidTaxIdException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTaxId(InvalidTaxIdException ex, HttpServletRequest request) {
        log.warn("Invalid tax identification: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "invalid-tax-id", "Invalid Tax ID", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(CustomerFiscalDataMissingException.class)
    public ResponseEntity<ProblemDetail> handleCustomerFiscalDataMissing(CustomerFiscalDataMissingException ex, HttpServletRequest request) {
        log.warn("Missing customer fiscal data: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "customer-fiscal-data-missing", "Missing Customer Fiscal Data", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(InvalidVoucherAmountException.class)
    public ResponseEntity<ProblemDetail> handleInvalidVoucherAmount(InvalidVoucherAmountException ex, HttpServletRequest request) {
        log.warn("Invalid voucher amount: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "invalid-voucher-amount", "Invalid Voucher Amount", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(VoucherAlreadyPaidException.class)
    public ResponseEntity<ProblemDetail> handleVoucherAlreadyPaid(VoucherAlreadyPaidException ex, HttpServletRequest request) {
        log.warn("Voucher already paid or payment overflow: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "voucher-already-paid", "Voucher Already Paid", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(VoucherNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleVoucherNotFound(VoucherNotFoundException ex, HttpServletRequest request) {
        log.warn("Electronic voucher not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "voucher-not-found", "Voucher Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(SeriesNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSeriesNotFound(SeriesNotFoundException ex, HttpServletRequest request) {
        log.warn("Fiscal series configuration not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "series-not-found", "Series Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(CreditNoteReferenceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleCreditNoteReferenceNotFound(CreditNoteReferenceNotFoundException ex, HttpServletRequest request) {
        log.warn("Credit note target voucher reference not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "credit-note-reference-not-found", "Credit Note Reference Not Found", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(CorrelativeExhaustedException.class)
    public ResponseEntity<ProblemDetail> handleCorrelativeExhausted(CorrelativeExhaustedException ex, HttpServletRequest request) {
        log.error("Fiscal series correlative exhausted: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "correlative-exhausted", "Correlative Exhausted", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(VoucherImmutableException.class)
    public ResponseEntity<ProblemDetail> handleVoucherImmutable(VoucherImmutableException ex, HttpServletRequest request) {
        log.warn("Attempt to mutate legally immutable voucher: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "voucher-immutable", "Voucher Legally Immutable", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(SunatIntegrationException.class)
    public ResponseEntity<ProblemDetail> handleSunatIntegration(SunatIntegrationException ex, HttpServletRequest request) {
        log.error("SUNAT/PSE integration failure: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_GATEWAY, "sunat-error", "SUNAT Integration Failure", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(InvoicingDomainException.class)
    public ResponseEntity<ProblemDetail> handleInvoicingDomain(InvoicingDomainException ex, HttpServletRequest request) {
        log.warn("Invoicing business rule violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "invoicing-business-rule", "Invoicing Rule Violation", ex.getMessage(), ex.errorCode(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Forbidden invoicing access attempt: {}", ex.getMessage());
        String defaultMsg = "Access denied: You do not possess the required fiscal authorities";
        String resolvedMsg = resolveMessageOrDefault("ERR_ACCESS_DENIED", defaultMsg);
        return buildResponse(HttpStatus.FORBIDDEN, "forbidden", "Forbidden", resolvedMsg, "ERR_ACCESS_DENIED", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error on invoicing request: {}", ex.getMessage());
        String defaultMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        return buildResponse(HttpStatus.BAD_REQUEST, "validation-error", "Validation Failed", defaultMsg, "VALIDATION_FAILED", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request body: {}", ex.getMessage());
        String defaultMsg = "Malformed or unreadable JSON payload";
        String resolvedMsg = resolveMessageOrDefault("MALFORMED_REQUEST", defaultMsg);
        return buildResponse(HttpStatus.BAD_REQUEST, "malformed-request", "Malformed Request", resolvedMsg, "MALFORMED_REQUEST", request);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ProblemDetail> handleIllegalArgument(RuntimeException ex, HttpServletRequest request) {
        log.warn("Illegal argument in invoicing request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "bad-request", "Bad Request", ex.getMessage(), "BAD_REQUEST", request);
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
        problem.setProperty("errorCode", errorCode != null ? errorCode : "INVOICING_ERROR");
        problem.setProperty("code", errorCode != null ? errorCode : "INVOICING_ERROR");
        problem.setProperty("timestamp", Instant.now());
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }

        return ResponseEntity.status(status).body(problem);
    }

    private String resolveTitleOrDefault(String typeSuffix, String defaultTitle) {
        if (typeSuffix == null) return defaultTitle;
        String mappedSuffix = switch (typeSuffix) {
            case "customer-fiscal-data-missing" -> "customer_fiscal_missing";
            case "sunat-error" -> "sunat_integration_failed";
            case "credit-note-reference-not-found" -> "credit_note_ref_not_found";
            default -> typeSuffix.replace('-', '_');
        };
        String key = "title." + mappedSuffix;
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
                "error.invoicing." + codeLower,
                "error.domain." + codeLower,
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
