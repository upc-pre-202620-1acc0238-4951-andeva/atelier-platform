package com.andeva.atelier.platform.billing.interfaces.acl;

import com.andeva.atelier.platform.billing.interfaces.acl.dto.FeatureEntitlementDto;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantSubscriptionStatusDto;

import java.util.UUID;

/**
 * Inbound Anti-Corruption Layer (ACL) and Open Host Service (OHS) exposing subscription
 * policies, status validations, and operational quotas to downstream bounded contexts.
 *
 * @author Joel Huamani Estefanero
 */
public interface SubscriptionContextFacade {

    /**
     * High-speed validation (< 0.05 ms via Caffeine In-Memory Cache) of active subscription status.
     *
     * @param tenantId workshop tenant identifier
     * @return true if the subscription is ACTIVE, TRIALING, or in PAST_DUE grace period
     */
    boolean isTenantSubscriptionActive(UUID tenantId);

    /**
     * Retrieves the effective operational quotas and feature limits for the tenant.
     *
     * @param tenantId workshop tenant identifier
     * @return TenantQuotaLimitsDto
     */
    TenantQuotaLimitsDto getTenantQuotaLimits(UUID tenantId);

    /**
     * Retrieves detailed subscription contract status for the tenant.
     *
     * @param tenantId workshop tenant identifier
     * @return TenantSubscriptionStatusDto
     */
    TenantSubscriptionStatusDto getTenantSubscriptionStatus(UUID tenantId);

    /**
     * Evaluates if the workshop is authorized to open an additional branch.
     *
     * @param tenantId           workshop tenant identifier
     * @param currentBranchCount current number of active branches
     * @return true if within allowed quotas
     */
    boolean canAddBranch(UUID tenantId, int currentBranchCount);

    /**
     * Evaluates if the workshop is authorized to recruit an additional staff member.
     *
     * @param tenantId          workshop tenant identifier
     * @param currentStaffCount current number of active staff members
     * @return true if within allowed quotas
     */
    boolean canAddStaffMember(UUID tenantId, int currentStaffCount);

    /**
     * Evaluates if the workshop is authorized to open a new monthly work order.
     *
     * @param tenantId                  workshop tenant identifier
     * @param currentMonthlyWorkOrders current work order count this month
     * @return true if within allowed quotas
     */
    boolean canCreateWorkOrder(UUID tenantId, int currentMonthlyWorkOrders);

    /**
     * Evaluates if an advanced functional feature toggle is enabled for the tenant.
     *
     * @param tenantId   workshop tenant identifier
     * @param featureKey feature toggle key
     * @return true if enabled and permitted
     */
    boolean isFeatureAllowed(UUID tenantId, String featureKey);

    /**
     * Checks entitlement status and usage limits for a specific feature key.
     *
     * @param tenantId   workshop tenant identifier
     * @param featureKey feature toggle key
     * @return FeatureEntitlementDto
     */
    FeatureEntitlementDto checkFeatureEntitlement(UUID tenantId, String featureKey);
}
