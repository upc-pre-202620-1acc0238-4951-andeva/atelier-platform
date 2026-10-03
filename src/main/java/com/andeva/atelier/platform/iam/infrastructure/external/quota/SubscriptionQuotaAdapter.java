package com.andeva.atelier.platform.iam.infrastructure.external.quota;

import com.andeva.atelier.platform.iam.application.internal.outbound.acl.SubscriptionQuotaPort;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Outbound Anti-Corruption Layer adapter implementing {@link SubscriptionQuotaPort}.
 * Enforces operational quotas on physical branches and staff members according to the workshop's subscription tier.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SubscriptionQuotaAdapter implements SubscriptionQuotaPort {

    private final int maxBranchesPerTenant;
    private final int maxStaffPerTenant;

    public SubscriptionQuotaAdapter(
            @Value("${subscription.default-quota.max-branches:10}") int maxBranchesPerTenant,
            @Value("${subscription.default-quota.max-staff:50}") int maxStaffPerTenant) {
        this.maxBranchesPerTenant = maxBranchesPerTenant;
        this.maxStaffPerTenant = maxStaffPerTenant;
    }

    @Override
    public void validateBranchCreationAllowed(TenantId tenantId, int currentBranchCount) {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        if (currentBranchCount >= maxBranchesPerTenant) {
            throw new IllegalStateException(
                    "Subscription tier limit exceeded: maximum allowed branches is " + maxBranchesPerTenant);
        }
    }

    @Override
    public void validateStaffAdditionAllowed(TenantId tenantId, int currentStaffCount) {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        if (currentStaffCount >= maxStaffPerTenant) {
            throw new IllegalStateException(
                    "Subscription tier limit exceeded: maximum allowed staff members is " + maxStaffPerTenant);
        }
    }
}
