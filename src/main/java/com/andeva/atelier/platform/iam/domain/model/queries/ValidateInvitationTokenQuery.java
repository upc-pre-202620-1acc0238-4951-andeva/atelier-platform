package com.andeva.atelier.platform.iam.domain.model.queries;

import java.util.Objects;

/**
 * Query requesting cryptographic validation of a prospective staff invitation token.
 *
 * @author Joel Huamani Estefanero
 */
public record ValidateInvitationTokenQuery(String token) {
    public ValidateInvitationTokenQuery {
        Objects.requireNonNull(token, "Token cannot be null");
        if (token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be blank");
        }
    }
}
