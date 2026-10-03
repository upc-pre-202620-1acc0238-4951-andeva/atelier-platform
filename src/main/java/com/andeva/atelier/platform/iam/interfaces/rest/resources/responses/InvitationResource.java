package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.time.Instant;
import java.util.UUID;

/**
 * Response projection representing a staff invitation.
 *
 * @param id        Universal unique identifier of the invitation
 * @param tenantId  Universal identifier of the issuing workshop tenant
 * @param email     Invitee's email address
 * @param status    Invitation status (PENDING, ACCEPTED, EXPIRED, REVOKED)
 * @param expiresAt Expiration instant in UTC
 * @author Joel Huamani Estefanero
 */
public record InvitationResource(
        UUID id,
        UUID tenantId,
        String email,
        String status,
        Instant expiresAt
) {
}
