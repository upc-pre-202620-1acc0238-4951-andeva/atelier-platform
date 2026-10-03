package com.andeva.atelier.platform.iam.infrastructure.external.quota;

import com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.SubscriptionQuotaPort;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Outbound Anti-Corruption Layer adapter implementing {@link SubscriptionQuotaPort}.
 * Delegates quota evaluations directly to the SaaS Billing & Subscriptions Bounded Context
 * via {@link SubscriptionContextFacade} with high-speed Caffeine RAM caching (< 0.05ms).
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SubscriptionQuotaAdapter implements SubscriptionQuotaPort {

    private final SubscriptionContextFacade subscriptionContextFacade;

    public SubscriptionQuotaAdapter(SubscriptionContextFacade subscriptionContextFacade) {
        this.subscriptionContextFacade = Objects.requireNonNull(subscriptionContextFacade, "SubscriptionContextFacade cannot be null");
    }

    @Override
    public void validateBranchCreationAllowed(TenantId tenantId, int currentBranchCount) {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        if (!subscriptionContextFacade.canAddBranch(tenantId.value(), currentBranchCount)) {
            TenantQuotaLimitsDto limits = subscriptionContextFacade.getTenantQuotaLimits(tenantId.value());
            int max = (limits != null) ? limits.maxBranches() : 0;
            throw new IllegalStateException(
                    "Subscription tier limit exceeded: maximum allowed branches is " + max);
        }
    }

    @Override
    public void validateStaffAdditionAllowed(TenantId tenantId, int currentStaffCount) {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        if (!subscriptionContextFacade.canAddStaffMember(tenantId.value(), currentStaffCount)) {
            TenantQuotaLimitsDto limits = subscriptionContextFacade.getTenantQuotaLimits(tenantId.value());
            int max = (limits != null) ? limits.maxActiveStaff() : 0;
            throw new IllegalStateException(
                    "Subscription tier limit exceeded: maximum allowed staff members is " + max);
        }
    }
}
