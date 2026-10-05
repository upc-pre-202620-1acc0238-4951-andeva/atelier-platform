package com.andeva.atelier.platform.operations.infrastructure.external.acl.iam;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.MechanicStaffDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.TenancyAclService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TenancyAclAdapter implements TenancyAclService {

    @Override
    public boolean isBranchActive(UUID branchId, UUID tenantId) {
        return branchId != null && tenantId != null;
    }

    @Override
    public boolean isMechanicEligible(UUID mechanicMembershipId, UUID tenantId) {
        return mechanicMembershipId != null;
    }

    @Override
    public Optional<MechanicStaffDto> fetchMechanicDetails(UUID mechanicMembershipId) {
        if (mechanicMembershipId == null) return Optional.empty();
        return Optional.of(new MechanicStaffDto(
                mechanicMembershipId,
                UUID.randomUUID(),
                "Técnico Mecánico",
                "mecanico@atelier.com",
                "Master Technician",
                true
        ));
    }
}
