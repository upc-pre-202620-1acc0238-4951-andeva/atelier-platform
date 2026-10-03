package com.andeva.atelier.platform.shared.application.result;

import java.util.List;
import java.util.Objects;

/**
 * Immutable record carrying semantic information regarding an anomalous condition or unsatisfied business rule.
 * Designed to decouple exception control from the application layer and provide clean metadata to web and presentation layers.
 *
 * @param code    standardized alphanumeric error code (e.g. NOT_FOUND, CONFLICT, BAD_REQUEST)
 * @param message human-readable message friendly for end-user display or diagnosis
 * @param details immutable list of specific field-level validation details
 * @author Joel Huamani Estefanero
 */
public record ApplicationError(
        String code,
        String message,
        List<String> details
) {

    /**
     * Compact constructor enforcing invariants and defensive copying.
     */
    public ApplicationError {
        Objects.requireNonNull(code, "Error code cannot be null");
        Objects.requireNonNull(message, "Error message cannot be null");
        details = details != null ? List.copyOf(details) : List.of();
    }

    /**
     * Creates a NOT_FOUND error for a specific resource and identifier.
     *
     * @param resource name of the missing resource
     * @param id       identifier of the missing resource
     * @return an ApplicationError representing not found
     */
    public static ApplicationError notFound(String resource, Object id) {
        return new ApplicationError("NOT_FOUND", String.format("%s with identifier %s was not found", resource, id), List.of());
    }

    /**
     * Creates a NOT_FOUND error with a custom message.
     *
     * @param message descriptive message
     * @return an ApplicationError representing not found
     */
    public static ApplicationError notFound(String message) {
        return new ApplicationError("NOT_FOUND", message, List.of());
    }

    /**
     * Creates a CONFLICT error with a descriptive message.
     *
     * @param message conflict explanation
     * @return an ApplicationError representing conflict
     */
    public static ApplicationError conflict(String message) {
        return new ApplicationError("CONFLICT", message, List.of());
    }

    /**
     * Creates a BAD_REQUEST error with a message.
     *
     * @param message error description
     * @return an ApplicationError representing bad request
     */
    public static ApplicationError badRequest(String message) {
        return new ApplicationError("BAD_REQUEST", message, List.of());
    }

    /**
     * Creates a BAD_REQUEST error with a message and a list of detailed field errors.
     *
     * @param message error description
     * @param details field-level validation error messages
     * @return an ApplicationError representing bad request
     */
    public static ApplicationError badRequest(String message, List<String> details) {
        return new ApplicationError("BAD_REQUEST", message, details);
    }

    /**
     * Creates an UNAUTHORIZED error.
     *
     * @param message authentication failure message
     * @return an ApplicationError representing unauthorized
     */
    public static ApplicationError unauthorized(String message) {
        return new ApplicationError("UNAUTHORIZED", message, List.of());
    }

    /**
     * Creates a FORBIDDEN error.
     *
     * @param message access restriction message
     * @return an ApplicationError representing forbidden
     */
    public static ApplicationError forbidden(String message) {
        return new ApplicationError("FORBIDDEN", message, List.of());
    }

    /**
     * Creates an UNPROCESSABLE_ENTITY error.
     *
     * @param message unprocessable entity explanation
     * @return an ApplicationError representing unprocessable entity
     */
    public static ApplicationError unprocessableEntity(String message) {
        return new ApplicationError("UNPROCESSABLE_ENTITY", message, List.of());
    }

    /**
     * Creates an INTERNAL_ERROR error.
     *
     * @param message unexpected system error description
     * @return an ApplicationError representing internal error
     */
    public static ApplicationError internalError(String message) {
        return new ApplicationError("INTERNAL_ERROR", message, List.of());
    }
}
