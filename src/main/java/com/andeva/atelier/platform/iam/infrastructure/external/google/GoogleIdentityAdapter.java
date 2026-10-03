package com.andeva.atelier.platform.iam.infrastructure.external.google;

import com.andeva.atelier.platform.iam.application.internal.dto.GoogleUserPayload;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.GoogleIdentityGateway;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

/**
 * Outbound adapter implementing {@link GoogleIdentityGateway} to cryptographically verify
 * Google Identity Services ID tokens using the official Google API Client SDK.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class GoogleIdentityAdapter implements GoogleIdentityGateway {

    private static final Logger log = LoggerFactory.getLogger(GoogleIdentityAdapter.class);

    private final String clientId;
    private final GoogleIdTokenVerifier verifier;

    public GoogleIdentityAdapter(@Value("${google.client-id:google_client_id_placeholder}") String clientId) {
        this.clientId = clientId;
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
        .setAudience(Collections.singletonList(clientId))
        .build();
    }

    @Override
    public Optional<GoogleUserPayload> verifyIdToken(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            return Optional.empty();
        }

        // Mock token fallback for local development and testing
        if (idTokenString.startsWith("mock-google-token-") || "google_client_id_placeholder".equals(clientId)) {
            log.info("[Mock Google SSO] Resolving mock token: {}", idTokenString);
            return Optional.of(new GoogleUserPayload(
                    "google-sub-mock-12345",
                    "user.mock@atelier.pe",
                    "Carlos",
                    "Mendoza",
                    "https://storage.atelier.pe/avatars/mock.png",
                    true
            ));
        }

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.warn("Google ID token verification returned null for provided token");
                return Optional.empty();
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
            String givenName = (String) payload.get("given_name");
            String familyName = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture");

            if (givenName == null) {
                givenName = payload.get("name") != null ? (String) payload.get("name") : "Google";
            }
            if (familyName == null) {
                familyName = "User";
            }

            return Optional.of(new GoogleUserPayload(
                    googleId,
                    email,
                    givenName,
                    familyName,
                    pictureUrl,
                    emailVerified
            ));
        } catch (Exception e) {
            log.error("Failed to cryptographically verify Google ID token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
