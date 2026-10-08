package com.andeva.atelier.platform.operations.infrastructure.external.acl.iam;

import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.MechanicStaffDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.TenancyAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Anti-Corruption Layer adapter integrating Workshop Operations with IAM and Tenancy.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class TenancyAclAdapter implements TenancyAclService {

    private final TenancyContextFacade tenancyContextFacade;

    public TenancyAclAdapter() {
        this(null);
    }

    @Autowired
    public TenancyAclAdapter(@Autowired(required = false) TenancyContextFacade tenancyContextFacade) {
        this.tenancyContextFacade = tenancyContextFacade;
    }

    @Override
    public boolean isBranchActive(UUID branchId, UUID tenantId) {
        if (branchId == null || tenantId == null) {
            return false;
        }
        if (tenancyContextFacade == null) {
            return true;
        }
        return tenancyContextFacade.fetchBranchById(branchId)
                .map(branch -> tenantId.equals(branch.tenantId()) && branch.active())
                .orElse(false);
    }

    @Override
    public boolean isMechanicEligible(UUID mechanicMembershipId, UUID tenantId) {
        if (mechanicMembershipId == null) {
            return false;
        }
        if (tenancyContextFacade == null) {
            return true;
        }
        if (tenantId != null) {
            return tenancyContextFacade.isUserStaffMemberOfTenant(mechanicMembershipId, tenantId);
        }
        return tenancyContextFacade.fetchUserById(mechanicMembershipId).isPresent();
    }

    @Override
    public Optional<MechanicStaffDto> fetchMechanicDetails(UUID mechanicMembershipId) {
        if (mechanicMembershipId == null) {
            return Optional.empty();
        }
        if (tenancyContextFacade == null) {
            return Optional.of(new MechanicStaffDto(
                    mechanicMembershipId,
                    UUID.randomUUID(),
                    "Técnico Mecánico",
                    "mecanico@atelier.com",
                    "Master Technician",
                    true
            ));
        }
        return tenancyContextFacade.fetchUserById(mechanicMembershipId)
                .map(u -> new MechanicStaffDto(
                        u.userId(),
                        u.userId(),
                        u.fullName(),
                        u.email(),
                        "Technician",
                        "ACTIVE".equalsIgnoreCase(u.status())
                ));
    }
}
