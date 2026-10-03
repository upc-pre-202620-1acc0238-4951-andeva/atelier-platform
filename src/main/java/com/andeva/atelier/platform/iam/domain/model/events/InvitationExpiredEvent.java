package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a staff onboarding invitation passes its validity deadline.
 *
 * @author Joel Huamani Estefanero
 */
public record InvitationExpiredEvent(
        InvitationId invitationId,
        Instant occurredOn
) implements Serializable {

    public InvitationExpiredEvent {
        Objects.requireNonNull(invitationId, "Invitation identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static InvitationExpiredEvent of(InvitationId invitationId) {
        return new InvitationExpiredEvent(invitationId, Instant.now());
    }
}
