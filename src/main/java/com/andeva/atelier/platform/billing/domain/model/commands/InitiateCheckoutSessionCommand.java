package com.andeva.atelier.platform.billing.domain.model.commands;

import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Command carrying parameters to initialize a hosted Stripe Checkout Session for a tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record InitiateCheckoutSessionCommand(
        TenantId tenantId,
        PlanId planId,
        String successUrl,
        String cancelUrl
) {

    public InitiateCheckoutSessionCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(planId, "PlanId cannot be null");
        Objects.requireNonNull(successUrl, "Success URL cannot be null");
        if (successUrl.isBlank()) {
            throw new IllegalArgumentException("Success URL cannot be blank");
        }
        Objects.requireNonNull(cancelUrl, "Cancel URL cannot be null");
        if (cancelUrl.isBlank()) {
            throw new IllegalArgumentException("Cancel URL cannot be blank");
        }
    }
}
