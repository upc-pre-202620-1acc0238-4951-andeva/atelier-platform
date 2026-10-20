package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.util.Optional;
import java.util.UUID;

public interface TenancyAclService {

    boolean isBranchActive(UUID branchId, UUID tenantId);

    boolean isMechanicEligible(UUID mechanicMembershipId, UUID tenantId);

    Optional<MechanicStaffDto> fetchMechanicDetails(UUID mechanicMembershipId);

    /**
     * Active staff membership of a user in the tenant (tasks are assigned to memberships, not users).
     */
    Optional<UUID> resolveActiveMembershipId(UUID userId, UUID tenantId);
}
