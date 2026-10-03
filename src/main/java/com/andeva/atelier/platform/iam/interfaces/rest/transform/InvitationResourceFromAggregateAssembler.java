package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.InvitationResource;

import java.util.Objects;

/**
 * Assembler projecting domain {@link Invitation} aggregates into REST {@link InvitationResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
public final class InvitationResourceFromAggregateAssembler {

    private InvitationResourceFromAggregateAssembler() {
    }

    /**
     * Converts an {@link Invitation} aggregate into an {@link InvitationResource}.
     *
     * @param invitation Domain invitation aggregate
     * @return REST response projection
     */
    public static InvitationResource toResourceFromAggregate(Invitation invitation) {
        Objects.requireNonNull(invitation, "Invitation aggregate cannot be null");
        return new InvitationResource(
                invitation.id().value(),
                invitation.tenantId().value(),
                invitation.email().value(),
                invitation.status().name(),
                invitation.expiresAt()
        );
    }
}
