package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

/**
 * Response projection returning validation metadata for a staff invitation token.
 *
 * @param valid      Flag indicating if the token is valid and redeemable
 * @param email      Registered invitee email address
 * @param tenantName Commercial name of the issuing workshop
 * @param roleName   Name of the proposed role to be assigned upon onboarding
 * @author Joel Huamani Estefanero
 */
public record InvitationValidationResource(
        boolean valid,
        String email,
        String tenantName,
        String roleName
) {
}
