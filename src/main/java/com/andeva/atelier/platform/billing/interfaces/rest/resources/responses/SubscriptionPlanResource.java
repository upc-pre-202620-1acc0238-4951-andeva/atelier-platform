package com.andeva.atelier.platform.billing.interfaces.rest.resources.responses;

import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST response resource providing full commercial and quota specifications of a subscription plan.
 *
 * @author Joel Huamani Estefanero
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionPlanResource(
        UUID id,
        String stripePriceId,
        String name,
        String tier,
        BigDecimal price,
        String currency,
        String billingCycle,
        TenantQuotaLimitsDto quotaLimits,
        List<PlanFeatureResource> features,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
