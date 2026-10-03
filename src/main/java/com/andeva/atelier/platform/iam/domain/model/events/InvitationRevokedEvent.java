package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a workshop administrator manually revokes a staff onboarding invitation.
 *
 * @author Joel Huamani Estefanero
 */
public record InvitationRevokedEvent(
        InvitationId invitationId,
        Instant occurredOn
) implements Serializable {

    public InvitationRevokedEvent {
        Objects.requireNonNull(invitationId, "Invitation identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static InvitationRevokedEvent of(InvitationId invitationId) {
        return new InvitationRevokedEvent(invitationId, Instant.now());
    }
}
