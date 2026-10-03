package com.andeva.atelier.platform.iam.infrastructure.security.tokens;

import com.andeva.atelier.platform.iam.application.internal.outbound.security.BearerTokenService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Infrastructure implementation of {@link BearerTokenService} utilizing JJWT 0.12.6
 * with HMAC-SHA256 signatures for stateless JWT token lifecycle management.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class BearerTokenServiceImpl implements BearerTokenService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public BearerTokenServiceImpl(
            @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secretKey,
            @Value("${jwt.expiration:86400000}") long expirationMs) {
        this.signingKey = resolveSigningKey(secretKey);
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user, TenantId tenantId, Collection<String> permissions) {
        Objects.requireNonNull(user, "User cannot be null");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        List<String> permList = permissions != null ? permissions.stream().toList() : List.of();

        return Jwts.builder()
                .subject(user.id().value().toString())
                .claim("email", user.email().value())
                .claim("tenantId", tenantId != null ? tenantId.value().toString() : null)
                .claim("permissions", permList)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public Map<String, Object> extractClaims(String token) {
        if (token == null || token.isBlank()) {
            return Map.of();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new HashMap<>(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Map.of();
        }
    }

    /**
     * Verifies whether the provided token has a valid cryptographic signature and is unexpired.
     *
     * @param token serialized JWT
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Resolves the secret key using Base64, Hex or raw UTF-8 bytes to ensure minimum 256 bits.
     */
    private static SecretKey resolveSigningKey(String secret) {
        Objects.requireNonNull(secret, "JWT secret cannot be null");
        byte[] keyBytes;
        try {
            if (secret.matches("^[0-9a-fA-F]{64,}$")) {
                // Hex-encoded 256-bit+ key
                keyBytes = hexStringToByteArray(secret);
            } else {
                keyBytes = Decoders.BASE64.decode(secret);
            }
        } catch (Exception e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
