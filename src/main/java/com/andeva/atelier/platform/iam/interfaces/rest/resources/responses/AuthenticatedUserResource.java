package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.util.List;
import java.util.UUID;

/**
 * Response projection representing an authenticated user session with enriched JWT Bearer token.
 *
 * @param userId       Universal unique identifier of the authenticated user
 * @param email        Verified user email address
 * @param fullName     Full legal name of the user
 * @param token        Signed JWT Bearer token
 * @param tokenType    Token scheme type (Bearer)
 * @param activeTenant Active workshop tenant associated with the session (or null)
 * @param permissions  Flat list of atomic permissions granted to the user in the active tenant
 * @author Joel Huamani Estefanero
 */
public record AuthenticatedUserResource(
        UUID userId,
        String email,
        String fullName,
        String token,
        String tokenType,
        TenantSummaryResource activeTenant,
        List<String> permissions
) {
    public AuthenticatedUserResource {
        permissions = permissions != null ? List.copyOf(permissions) : List.of();
    }
}
