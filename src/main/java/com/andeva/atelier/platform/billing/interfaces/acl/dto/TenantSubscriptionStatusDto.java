package com.andeva.atelier.platform.billing.interfaces.acl.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable DTO providing detailed subscription contract status for a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantSubscriptionStatusDto(
        UUID tenantId,
        String planName,
        String tier,
        String status,
        boolean isActive,
        Instant currentPeriodEnd,
        boolean cancelAtPeriodEnd
) implements Serializable {
}
