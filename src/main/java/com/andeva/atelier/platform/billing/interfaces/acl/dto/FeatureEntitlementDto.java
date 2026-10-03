package com.andeva.atelier.platform.billing.interfaces.acl.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Immutable DTO verifying rights and entitlements for a specific functional module.
 *
 * @author Joel Huamani Estefanero
 */
public record FeatureEntitlementDto(
        UUID tenantId,
        String featureKey,
        boolean isEntitled,
        int currentUsage,
        int maximumLimit
) implements Serializable {
}
