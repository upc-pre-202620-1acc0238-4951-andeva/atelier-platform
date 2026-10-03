package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a staff member onboarding invitation is dispatched.
 *
 * @author Joel Huamani Estefanero
 */
public record StaffInvitedEvent(
        InvitationId invitationId,
        TenantId tenantId,
        EmailAddress email,
        String token,
        Instant occurredOn
) implements Serializable {

    public StaffInvitedEvent {
        Objects.requireNonNull(invitationId, "Invitation identifier cannot be null");
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(email, "Target email address cannot be null");
        Objects.requireNonNull(token, "Invitation security token cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static StaffInvitedEvent of(
            InvitationId invitationId,
            TenantId tenantId,
            EmailAddress email,
            String token) {
        return new StaffInvitedEvent(invitationId, tenantId, email, token, Instant.now());
    }
}
