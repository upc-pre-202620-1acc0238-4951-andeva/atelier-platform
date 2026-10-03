package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a User's password has been successfully updated.
 *
 * @author Joel Huamani Estefanero
 */
public record PasswordChangedEvent(
        UserId userId,
        Instant occurredOn
) implements Serializable {

    public PasswordChangedEvent {
        Objects.requireNonNull(userId, "User identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static PasswordChangedEvent of(UserId userId) {
        return new PasswordChangedEvent(userId, Instant.now());
    }
}
