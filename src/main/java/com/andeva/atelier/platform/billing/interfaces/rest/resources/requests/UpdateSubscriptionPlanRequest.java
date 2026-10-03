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

/**
 * Administrative request payload to update details, pricing, cycle, or quota limits of an existing plan.
 *
 * @author Joel Huamani Estefanero
 */
public record UpdateSubscriptionPlanRequest(
        @NotBlank(message = "{billing.validation.plan.name.required}")
        @Size(min = 3, max = 100, message = "{billing.validation.plan.name.size}")
        String name,

        @NotNull(message = "{billing.validation.plan.price.required}")
        @DecimalMin(value = "0.0", inclusive = true, message = "{billing.validation.plan.price.min}")
        @Digits(integer = 10, fraction = 2, message = "{billing.validation.plan.price.digits}")
        BigDecimal price,

        @NotBlank(message = "{billing.validation.plan.billing_cycle.required}")
        @Pattern(regexp = "^(MONTHLY|YEARLY)$", message = "{billing.validation.plan.billing_cycle.invalid}")
        String billingCycle,

        @NotNull(message = "{billing.validation.plan.quota_limits.required}")
        @Valid
        TenantQuotaLimitsDto quotaLimits,

        Boolean isActive
) {
}
