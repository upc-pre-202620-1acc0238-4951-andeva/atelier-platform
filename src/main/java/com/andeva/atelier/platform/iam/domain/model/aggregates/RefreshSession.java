package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Long-lived session of a signed-in device. Only the SHA-256 hash of the opaque refresh token is
 * kept; each refresh rotates the token, and signing out revokes it.
 *
 * @author Joel Huamani Estefanero
 */
public class RefreshSession {

    private final UUID id;
    private final UserId userId;
    private final String tokenHash;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private Instant revokedAt;

    public RefreshSession(UUID id, UserId userId, String tokenHash, Instant issuedAt, Instant expiresAt, Instant revokedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash cannot be null");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        this.revokedAt = revokedAt;
    }

    public static RefreshSession issue(UserId userId, String tokenHash, Duration validity) {
        Instant now = Instant.now();
        return new RefreshSession(UUID.randomUUID(), userId, tokenHash, now, now.plus(validity), null);
    }

    public boolean isUsable() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }

    public void revoke() {
        if (revokedAt == null) {
            revokedAt = Instant.now();
        }
    }

    public UUID id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant revokedAt() {
        return revokedAt;
    }
}
