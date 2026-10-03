package com.andeva.atelier.platform.iam.application.internal.outbound.acl;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Outbound Anti-Corruption Layer port for verifying workshop subscription plan quotas.
 *
 * @author Joel Huamani Estefanero
 */
public interface SubscriptionQuotaPort {

    /**
     * Asserts that the workshop tenant is legally permitted to create an additional physical branch.
     *
     * @param tenantId the workshop tenant identifier
     * @param currentBranchCount current number of branches configured for the tenant
     */
    void validateBranchCreationAllowed(TenantId tenantId, int currentBranchCount);

    /**
     * Asserts that the workshop tenant is legally permitted to invite an additional staff member.
     *
     * @param tenantId the workshop tenant identifier
     * @param currentStaffCount current number of staff members in the tenant
     */
    void validateStaffAdditionAllowed(TenantId tenantId, int currentStaffCount);
}
