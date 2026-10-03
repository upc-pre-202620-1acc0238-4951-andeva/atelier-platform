package com.andeva.atelier.platform.shared.interfaces.rest.transform;

import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.interfaces.rest.resources.ErrorResource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Utility assembler transforming application layer anomalies ({@link ApplicationError})
 * into standardized RFC 7807 HTTP responses ({@link ResponseEntity})
 * with dynamic internationalization (i18n) based on the client's locale.
 *
 * @author Joel Huamani Estefanero
 */
public final class ErrorResponseAssembler {

    private static final String MESSAGES_BASENAME = "messages";

    private ErrorResponseAssembler() {
    }

    /**
     * Maps an ApplicationError to a standardized ResponseEntity containing an ErrorResource,
     * evaluating its semantic error code and resolving localized messages.
     *
     * @param error application error descriptor
     * @return structured HTTP error response
     */
    public static ResponseEntity<ErrorResource> toErrorResponseFromApplicationError(ApplicationError error) {
        Objects.requireNonNull(error, "ApplicationError cannot be null");

        HttpStatusCode status = toStatusFromErrorCode(error.code());
        String localizedMessage = toLocalizedMessage(error);
        ErrorResource resource = ErrorResource.of(error.code(), localizedMessage, error.details());
        return new ResponseEntity<>(resource, status);
    }

    /**
     * Resolves the localized message from the active ResourceBundle using LocaleContextHolder,
     * falling back to the default message provided in ApplicationError.
     */
    private static String toLocalizedMessage(ApplicationError error) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, locale);
            String codeLower = error.code().toLowerCase(Locale.ROOT);
            java.util.List<String> candidates = java.util.List.of(
                    "error.domain." + codeLower,
                    "error.application." + codeLower,
                    "error." + codeLower.replace('_', '.'),
                    error.code()
            );
            for (String key : candidates) {
                if (bundle.containsKey(key)) {
                    Object[] args = !error.details().isEmpty()
                            ? error.details().toArray()
                            : new Object[]{error.message()};
                    return MessageFormat.format(bundle.getString(key), args);
                }
            }
        } catch (MissingResourceException ignored) {
            // Fallback to error message
        }
        return error.message();
    }

    /**
     * Determines the appropriate HttpStatusCode based on the standardized error code.
     *
     * @param errorCode error code string
     * @return corresponding HttpStatusCode
     */
    public static HttpStatusCode toStatusFromErrorCode(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return switch (errorCode) {
            case "NOT_FOUND", "RESOURCE_NOT_FOUND", "ENTITY_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONFLICT", "ALREADY_EXISTS", "DUPLICATE_RESOURCE" -> HttpStatus.CONFLICT;
            case "BAD_REQUEST", "VALIDATION_FAILED", "VALIDATION_ERROR", "INVALID_ARGUMENT" -> HttpStatus.BAD_REQUEST;
            case "UNAUTHORIZED", "INVALID_CREDENTIALS", "TOKEN_EXPIRED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN", "ACCESS_DENIED", "SUBSCRIPTION_REQUIRED" -> HttpStatus.FORBIDDEN;
            case "UNPROCESSABLE_ENTITY", "BUSINESS_RULE_VIOLATION", "INSUFFICIENT_STOCK", "CURRENCY_MISMATCH" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case String s when s.endsWith("_NOT_FOUND") -> HttpStatus.NOT_FOUND;
            case String s when s.endsWith("_CONFLICT") -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
