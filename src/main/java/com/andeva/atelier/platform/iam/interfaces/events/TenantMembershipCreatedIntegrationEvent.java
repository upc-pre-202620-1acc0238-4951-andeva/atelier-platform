package com.andeva.atelier.platform.iam.interfaces.events;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a user is affiliated as an employee/member of a tenant.
 *
 * @param membershipId Universal unique identifier of the tenant membership
 * @param tenantId     Universal unique identifier of the employer tenant
 * @param userId       Universal unique identifier of the affiliated user
 * @param roleNames    List of role canonical names assigned to the member
 * @param occurredOn   Timestamp of membership creation
 * @author Joel Huamani Estefanero
 */
public record TenantMembershipCreatedIntegrationEvent(
        UUID membershipId,
        UUID tenantId,
        UUID userId,
        List<String> roleNames,
        Instant occurredOn
) {
    public TenantMembershipCreatedIntegrationEvent {
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        roleNames = roleNames != null ? List.copyOf(roleNames) : List.of();
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
