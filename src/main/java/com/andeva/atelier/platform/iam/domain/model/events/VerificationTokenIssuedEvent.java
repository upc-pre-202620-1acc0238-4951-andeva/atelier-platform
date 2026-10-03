package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a one-time verification token (email validation or OTP) is generated.
 *
 * @author Joel Huamani Estefanero
 */
public record VerificationTokenIssuedEvent(
        UserId userId,
        String tokenValue,
        TokenType type,
        Instant expiresAt,
        Instant occurredOn
) implements Serializable {

    public VerificationTokenIssuedEvent {
        Objects.requireNonNull(userId, "User identifier cannot be null");
        Objects.requireNonNull(tokenValue, "Token value cannot be null");
        Objects.requireNonNull(type, "Token type cannot be null");
        Objects.requireNonNull(expiresAt, "Expiration instant cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static VerificationTokenIssuedEvent of(
            UserId userId,
            String tokenValue,
            TokenType type,
            Instant expiresAt) {
        return new VerificationTokenIssuedEvent(userId, tokenValue, type, expiresAt, Instant.now());
    }
}
