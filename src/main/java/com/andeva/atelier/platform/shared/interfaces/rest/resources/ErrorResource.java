package com.andeva.atelier.platform.shared.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Standardized immutable REST error resource compliant with RFC 7807 (Problem Details).
 * Carries semantic error code, human-readable message, granular validation details, and occurrence timestamp.
 *
 * @param code      standardized alphanumeric error code
 * @param message   descriptive error message
 * @param details   list of granular field errors
 * @param timestamp UTC instant when the error was registered
 * @author Joel Huamani Estefanero
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResource(
        String code,
        String message,
        List<String> details,
        Instant timestamp
) {

    public ErrorResource {
        Objects.requireNonNull(code, "Error code cannot be null");
        Objects.requireNonNull(message, "Error message cannot be null");
        details = details != null ? List.copyOf(details) : List.of();
        timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static ErrorResource of(String code, String message) {
        return new ErrorResource(code, message, List.of(), Instant.now());
    }

    public static ErrorResource of(String code, String message, List<String> details) {
        return new ErrorResource(code, message, details, Instant.now());
    }
}
