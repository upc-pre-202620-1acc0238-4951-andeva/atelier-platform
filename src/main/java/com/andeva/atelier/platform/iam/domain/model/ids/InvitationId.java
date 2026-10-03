package com.andeva.atelier.platform.iam.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a Staff Onboarding Invitation.
 *
 * @author Joel Huamani Estefanero
 */
public record InvitationId(UUID value) implements Serializable {

    public InvitationId {
        Objects.requireNonNull(value, "Invitation identifier cannot be null");
    }

    public static InvitationId of(UUID value) {
        return new InvitationId(value);
    }

    public static InvitationId of(String value) {
        Objects.requireNonNull(value, "Invitation identifier string cannot be null");
        return new InvitationId(UUID.fromString(value));
    }

    public static InvitationId generate() {
        return new InvitationId(UUID.randomUUID());
    }
}
