package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a User account is suspended.
 *
 * @author Joel Huamani Estefanero
 */
public record UserSuspendedEvent(
        UserId userId,
        Instant occurredOn
) implements Serializable {

    public UserSuspendedEvent {
        Objects.requireNonNull(userId, "User identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static UserSuspendedEvent of(UserId userId) {
        return new UserSuspendedEvent(userId, Instant.now());
    }
}
