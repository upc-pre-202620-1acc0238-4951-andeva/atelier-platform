package com.andeva.atelier.platform.iam.application.internal.outbound.acl;

import com.andeva.atelier.platform.iam.application.internal.dto.GoogleUserPayload;

import java.util.Optional;

/**
 * Outbound Anti-Corruption Layer port for verifying Google Identity ID tokens.
 *
 * @author Joel Huamani Estefanero
 */
public interface GoogleIdentityGateway {

    /**
     * Cryptographically verifies a Google ID token and extracts the authenticated identity payload.
     *
     * @param idTokenString the raw JWT ID token emitted by Google Identity Services
     * @return Optional containing the verified user payload, or empty if verification fails
     */
    Optional<GoogleUserPayload> verifyIdToken(String idTokenString);
}
