package com.andeva.atelier.platform.shared.interfaces.rest;

import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;
import com.andeva.atelier.platform.shared.interfaces.rest.resources.ErrorResource;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Global REST controller advice (@RestControllerAdvice) intercepting web anomalies,
 * Bean Validation constraint violations, domain invariant breaches, and unhandled exceptions.
 * Translates all exceptions into standardized RFC 7807 ErrorResource responses with
 * dynamic internationalization (i18n) via Spring's LocaleContextHolder and ResourceBundle.
 *
 * @author Joel Huamani Estefanero
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MESSAGES_BASENAME = "messages";

    /**
     * Handles Jakarta Bean Validation errors (@Valid) on incoming request DTO bodies.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResource> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();

        log.warn("Validation failure in REST request: {} errors detected", details.size());
        String message = resolveMessageOrDefault(
                "error.application.bad_request",
                "The submitted request payload violates field validation constraints"
        );
        ErrorResource errorResource = ErrorResource.of("VALIDATION_FAILED", message, details);
        return new ResponseEntity<>(errorResource, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violations on URL or query parameters (@RequestParam, @PathVariable).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResource> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(v -> "%s: %s".formatted(v.getPropertyPath(), v.getMessage()))
                .toList();

        log.warn("Constraint violation on parameter: {}", ex.getMessage());
        String message = resolveMessageOrDefault(
                "error.application.bad_request",
                "One or more request parameters violate constraint rules"
        );
        ErrorResource errorResource = ErrorResource.of("CONSTRAINT_VIOLATION", message, details);
        return new ResponseEntity<>(errorResource, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles domain business invariant exceptions thrown by aggregate roots or value objects.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResource> handleDomainException(DomainException ex) {
        log.warn("Domain business invariant violated: [{}] {}", ex.errorCode(), ex.getMessage());

        String messageKey = "error.domain." + ex.errorCode().toLowerCase(Locale.ROOT);
        String resolvedMessage = resolveMessageOrDefault(messageKey, ex.getMessage());

        ErrorResource errorResource = ErrorResource.of(ex.errorCode(), resolvedMessage);
        return new ResponseEntity<>(errorResource, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handles illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResource> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument in request processing: {}", ex.getMessage());
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid argument passed";
        ErrorResource errorResource = ErrorResource.of("BAD_REQUEST", message);
        return new ResponseEntity<>(errorResource, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles malformed JSON payloads or incompatible deserialization types.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResource> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed HTTP request body: {}", ex.getMessage());
        String message = resolveMessageOrDefault(
                "error.application.bad_request",
                "Request body contains invalid JSON syntax or incompatible data types"
        );
        ErrorResource errorResource = ErrorResource.of("MALFORMED_JSON_REQUEST", message);
        return new ResponseEntity<>(errorResource, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles unsupported HTTP methods.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResource> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("HTTP method not supported: {}", ex.getMethod());
        String message = "HTTP method %s is not supported for this endpoint".formatted(ex.getMethod());
        ErrorResource errorResource = ErrorResource.of("METHOD_NOT_ALLOWED", message);
        return new ResponseEntity<>(errorResource, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles unsupported Media-Types (Content-Type).
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResource> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("HTTP media type not supported: {}", ex.getContentType());
        String message = "Supplied Content-Type is not supported by this endpoint";
        ErrorResource errorResource = ErrorResource.of("UNSUPPORTED_MEDIA_TYPE", message);
        return new ResponseEntity<>(errorResource, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Fallback catch-all handler for unexpected system exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResource> handleUnhandledException(Exception ex) {
        String correlationId = MDC.get("correlationId");
        log.error("Unhandled server exception [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

        String message = resolveMessageOrDefault(
                "error.application.internal_error",
                "An unexpected internal error occurred. Please contact system support citing correlation ID: " + correlationId
        );
        ErrorResource errorResource = ErrorResource.of("INTERNAL_SERVER_ERROR", message);
        return new ResponseEntity<>(errorResource, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String formatFieldError(FieldError fieldError) {
        return "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private String resolveMessageOrDefault(String key, String defaultValue, Object... args) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, LocaleContextHolder.getLocale());
            if (!bundle.containsKey(key)) {
                return defaultValue;
            }
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException ex) {
            return defaultValue;
        }
    }
}
