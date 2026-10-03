package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a User account is activated.
 *
 * @author Joel Huamani Estefanero
 */
public record UserActivatedEvent(
        UserId userId,
        Instant occurredOn
) implements Serializable {

    public UserActivatedEvent {
        Objects.requireNonNull(userId, "User identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static UserActivatedEvent of(UserId userId) {
        return new UserActivatedEvent(userId, Instant.now());
    }
}
