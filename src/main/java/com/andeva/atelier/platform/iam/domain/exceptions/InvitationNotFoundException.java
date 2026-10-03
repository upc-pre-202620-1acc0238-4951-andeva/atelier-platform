package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;

/**
 * Thrown when a staff onboarding invitation cannot be located by identifier or token.
 *
 * @author Joel Huamani Estefanero
 */
public class InvitationNotFoundException extends IamDomainException {

    public InvitationNotFoundException(InvitationId invitationId) {
        super("INVITATION_NOT_FOUND", "Invitation not found with identifier: " + (invitationId != null ? invitationId.value() : "null"));
    }

    public InvitationNotFoundException(String message) {
        super("INVITATION_NOT_FOUND", message);
    }
}
