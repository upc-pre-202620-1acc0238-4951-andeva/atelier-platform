package com.andeva.atelier.platform.iam.application.internal.commandservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.RefreshSession;
import com.andeva.atelier.platform.iam.domain.repositories.RefreshSessionRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;

/**
 * Issues, resolves and revokes the opaque refresh tokens of signed-in devices.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class RefreshSessionManager {

    private static final Duration REFRESH_VALIDITY = Duration.ofDays(30);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshSessionRepository sessionRepository;

    public RefreshSessionManager(RefreshSessionRepository sessionRepository) {
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository cannot be null");
    }

    /**
     * @return the raw refresh token; it is never stored, only its hash
     */
    public String issue(UserId userId) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessionRepository.save(RefreshSession.issue(userId, hash(rawToken), REFRESH_VALIDITY));
        return rawToken;
    }

    public Optional<RefreshSession> findUsable(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return sessionRepository.findByTokenHash(hash(rawToken.trim())).filter(RefreshSession::isUsable);
    }

    public void revoke(RefreshSession session) {
        session.revoke();
        sessionRepository.save(session);
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
