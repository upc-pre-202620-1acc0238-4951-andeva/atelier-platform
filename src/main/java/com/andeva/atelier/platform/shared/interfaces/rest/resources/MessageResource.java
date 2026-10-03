package com.andeva.atelier.platform.shared.interfaces.rest.resources;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable representation for simple operational confirmation messages or asynchronous
 * command acknowledgments that do not return a substantial payload.
 *
 * @param message   explanatory confirmation message
 * @param timestamp UTC instant when the message was generated
 * @author Joel Huamani Estefanero
 */
public record MessageResource(
        String message,
        Instant timestamp
) {

    public MessageResource {
        Objects.requireNonNull(message, "Message cannot be null");
        timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static MessageResource of(String message) {
        return new MessageResource(message, Instant.now());
    }
}
