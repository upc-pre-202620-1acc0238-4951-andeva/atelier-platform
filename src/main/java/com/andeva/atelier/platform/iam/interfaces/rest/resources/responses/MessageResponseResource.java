package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.time.Instant;

/**
 * Generic response projection carrying an informational or confirmation message and a UTC timestamp.
 *
 * @param message   Informational or operational result message
 * @param timestamp Instant of message emission in UTC
 * @author Joel Huamani Estefanero
 */
public record MessageResponseResource(
        String message,
        Instant timestamp
) {
    public MessageResponseResource(String message) {
        this(message, Instant.now());
    }
}
