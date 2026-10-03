package com.andeva.atelier.platform.billing.interfaces.rest.resources.requests;

import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Administrative request payload to create a new commercial subscription plan linked to a Stripe Price ID.
 *
 * @author Joel Huamani Estefanero
 */
public record CreateSubscriptionPlanRequest(
        @NotBlank(message = "{billing.validation.plan.stripe_price_id.required}")
        @Pattern(regexp = "^price_[a-zA-Z0-9_]+$", message = "{billing.validation.plan.stripe_price_id.format}")
        String stripePriceId,

        @NotBlank(message = "{billing.validation.plan.name.required}")
        @Size(min = 3, max = 100, message = "{billing.validation.plan.name.size}")
        String name,

        @NotBlank(message = "{billing.validation.plan.tier.required}")
        @Pattern(regexp = "^(GO|PRO|MAX|ENTERPRISE)$", message = "{billing.validation.plan.tier.invalid}")
        String tier,

        @NotNull(message = "{billing.validation.plan.price.required}")
        @DecimalMin(value = "0.0", inclusive = true, message = "{billing.validation.plan.price.min}")
        @Digits(integer = 10, fraction = 2, message = "{billing.validation.plan.price.digits}")
        BigDecimal price,

        @NotBlank(message = "{billing.validation.plan.currency.required}")
        @Size(min = 3, max = 3, message = "{billing.validation.plan.currency.size}")
        String currency,

        @NotBlank(message = "{billing.validation.plan.billing_cycle.required}")
        @Pattern(regexp = "^(MONTHLY|YEARLY)$", message = "{billing.validation.plan.billing_cycle.invalid}")
        String billingCycle,

        @NotNull(message = "{billing.validation.plan.quota_limits.required}")
        @Valid
        TenantQuotaLimitsDto quotaLimits,

        @Valid
        List<PlanFeatureRequest> features
) {
    public CreateSubscriptionPlanRequest(
            String stripePriceId,
            String name,
            String tier,
            BigDecimal price,
            String currency,
            String billingCycle,
            TenantQuotaLimitsDto quotaLimits
    ) {
        this(stripePriceId, name, tier, price, currency, billingCycle, quotaLimits, List.of());
    }
}
