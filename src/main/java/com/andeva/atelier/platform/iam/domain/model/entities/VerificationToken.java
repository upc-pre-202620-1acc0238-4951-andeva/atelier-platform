package com.andeva.atelier.platform.iam.domain.model.entities;

import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Transactional security token entity (email OTP, password reset, login challenge)
 * managed under the User aggregate.
 *
 * @author Joel Huamani Estefanero
 */
public class VerificationToken {

    private final UUID id;
    private final UserId userId;
    private final String tokenValue;
    private final TokenType type;
    private final Instant expiresAt;
    private boolean used;

    public VerificationToken(
            UUID id,
            UserId userId,
            String tokenValue,
            TokenType type,
            Instant expiresAt,
            boolean used) {
        this.id = Objects.requireNonNull(id, "Token identifier cannot be null");
        this.userId = Objects.requireNonNull(userId, "User identifier cannot be null");
        this.tokenValue = validateTokenValue(tokenValue);
        this.type = Objects.requireNonNull(type, "Token type cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration instant cannot be null");
        this.used = used;
    }

    public static VerificationToken issue(
            UserId userId,
            String tokenValue,
            TokenType type,
            Instant expiresAt) {
        return new VerificationToken(
                UUID.randomUUID(),
                userId,
                tokenValue,
                type,
                expiresAt,
                false
        );
    }

    /**
     * Determines whether the token is currently unspent and within its validity window.
     */
    public boolean isValid() {
        return !used && expiresAt.isAfter(Instant.now());
    }

    /**
     * Marks the token as consumed, preventing further reuse.
     */
    public void consume() {
        this.used = true;
    }

    public UUID id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public String tokenValue() {
        return tokenValue;
    }

    public TokenType type() {
        return type;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    private static String validateTokenValue(String tokenValue) {
        Objects.requireNonNull(tokenValue, "Token value cannot be null");
        String trimmed = tokenValue.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Token value cannot be empty");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerificationToken that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
