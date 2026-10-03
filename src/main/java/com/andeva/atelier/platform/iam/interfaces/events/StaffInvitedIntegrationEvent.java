package com.andeva.atelier.platform.iam.interfaces.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when an invitation is dispatched to prospective workshop staff.
 *
 * @param invitationId Universal unique identifier of the invitation
 * @param tenantId     Universal unique identifier of the inviting tenant
 * @param email        Destination email address of the invited staff member
 * @param occurredOn   Timestamp of event occurrence
 * @author Joel Huamani Estefanero
 */
public record StaffInvitedIntegrationEvent(
        UUID invitationId,
        UUID tenantId,
        String email,
        Instant occurredOn
) {
    public StaffInvitedIntegrationEvent {
        Objects.requireNonNull(invitationId, "invitationId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(email, "email cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
