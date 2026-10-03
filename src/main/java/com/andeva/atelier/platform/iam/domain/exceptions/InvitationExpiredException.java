package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;

/**
 * Thrown when an attempt is made to accept an invitation that has expired or is no longer pending.
 *
 * @author Joel Huamani Estefanero
 */
public class InvitationExpiredException extends IamDomainException {

    public InvitationExpiredException(InvitationId invitationId) {
        super("INVITATION_EXPIRED", "Invitation " + (invitationId != null ? invitationId.value() : "null")
                + " has expired or has already been accepted/revoked");
    }

    public InvitationExpiredException(String message) {
        super("INVITATION_EXPIRED", message);
    }
}
