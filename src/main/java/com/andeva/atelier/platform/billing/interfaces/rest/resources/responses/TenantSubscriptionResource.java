package com.andeva.atelier.platform.billing.interfaces.rest.resources.responses;

import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * REST response resource representing the complete contractual and quota status of a tenant's subscription.
 *
 * @author Joel Huamani Estefanero
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TenantSubscriptionResource(
        UUID id,
        UUID tenantId,
        UUID planId,
        String planName,
        String tier,
        String status,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        boolean cancelAtPeriodEnd,
        Instant trialEndDate,
        TenantQuotaLimitsDto quotaLimits,
        boolean isAccessGranted
) {
}
