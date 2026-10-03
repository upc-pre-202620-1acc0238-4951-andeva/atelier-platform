package com.andeva.atelier.platform.iam.application.internal.outbound.security;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Collection;
import java.util.Map;

/**
 * Outbound port for cryptographic JWT Bearer token generation and claims resolution.
 *
 * @author Joel Huamani Estefanero
 */
public interface BearerTokenService {

    /**
     * Generates a signed Bearer JWT token enriched with workshop context and resolved RBAC permissions.
     *
     * @param user the authenticated domain user
     * @param tenantId the active workshop tenant identifier
     * @param permissions collection of atomic permission codes granted to the user
     * @return the serialized signed JWT Bearer token
     */
    String generateToken(User user, TenantId tenantId, Collection<String> permissions);

    /**
     * Extracts all claims embedded in a signed JWT Bearer token.
     *
     * @param token the serialized JWT string
     * @return map of decoded token claims
     */
    Map<String, Object> extractClaims(String token);
}
