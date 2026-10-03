package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a password reset token is requested for a User account.
 *
 * @author Joel Huamani Estefanero
 */
public record PasswordResetRequestedEvent(
        UserId userId,
        EmailAddress email,
        String tokenValue,
        Instant expiresAt,
        Instant occurredOn
) implements Serializable {

    public PasswordResetRequestedEvent {
        Objects.requireNonNull(userId, "User identifier cannot be null");
        Objects.requireNonNull(email, "Email address cannot be null");
        Objects.requireNonNull(tokenValue, "Token value cannot be null");
        Objects.requireNonNull(expiresAt, "Expiration instant cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static PasswordResetRequestedEvent of(
            UserId userId,
            EmailAddress email,
            String tokenValue,
            Instant expiresAt) {
        return new PasswordResetRequestedEvent(userId, email, tokenValue, expiresAt, Instant.now());
    }
}
