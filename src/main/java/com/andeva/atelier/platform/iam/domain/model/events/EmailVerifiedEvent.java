package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a User's email address is successfully validated through OTP token.
 *
 * @author Joel Huamani Estefanero
 */
public record EmailVerifiedEvent(
        UserId userId,
        Instant occurredOn
) implements Serializable {

    public EmailVerifiedEvent {
        Objects.requireNonNull(userId, "User identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static EmailVerifiedEvent of(UserId userId) {
        return new EmailVerifiedEvent(userId, Instant.now());
    }
}
