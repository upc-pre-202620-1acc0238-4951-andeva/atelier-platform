package com.andeva.atelier.platform.iam.application.internal.dto;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Encapsulates the authenticated session payload produced upon successful sign-in.
 *
 * @author Joel Huamani Estefanero
 */
public record AuthenticatedUser(
        User user,
        String token,
        TenantId tenantId,
        Set<String> permissions
) implements Serializable {

    public AuthenticatedUser {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(token, "Bearer token cannot be null");
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }
}
