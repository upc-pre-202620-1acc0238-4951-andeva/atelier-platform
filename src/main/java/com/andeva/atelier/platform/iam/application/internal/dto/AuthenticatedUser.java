package com.andeva.atelier.platform.iam.application.internal.dto;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Encapsulates the authenticated session payload produced upon successful sign-in.
 *
 * @param membershipId active staff membership in the session tenant (or null), used by the staff app for its own records
 * @author Joel Huamani Estefanero
 */
public record AuthenticatedUser(
        User user,
        String token,
        TenantId tenantId,
        Set<String> permissions,
        String refreshToken,
        UUID membershipId
) implements Serializable {

    public AuthenticatedUser {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(token, "Bearer token cannot be null");
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    public AuthenticatedUser(User user, String token, TenantId tenantId, Set<String> permissions, String refreshToken) {
        this(user, token, tenantId, permissions, refreshToken, null);
    }

    public AuthenticatedUser(User user, String token, TenantId tenantId, Set<String> permissions) {
        this(user, token, tenantId, permissions, null, null);
    }

    public AuthenticatedUser withRefreshToken(String newRefreshToken) {
        return new AuthenticatedUser(user, token, tenantId, permissions, newRefreshToken, membershipId);
    }

    public AuthenticatedUser withMembershipId(UUID activeMembershipId) {
        return new AuthenticatedUser(user, token, tenantId, permissions, refreshToken, activeMembershipId);
    }
}
