package com.andeva.atelier.platform.hr.application.internal.queryservices;

import com.andeva.atelier.platform.hr.application.internal.outbound.acl.TenancyGeofenceAclService;
import com.andeva.atelier.platform.hr.application.queryservices.StaffMembershipQueryService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class StaffMembershipQueryServiceImpl implements StaffMembershipQueryService {

    private final TenancyGeofenceAclService tenancyAclService;

    public StaffMembershipQueryServiceImpl(TenancyGeofenceAclService tenancyAclService) {
        this.tenancyAclService = Objects.requireNonNull(tenancyAclService, "tenancyAclService cannot be null");
    }

    @Override
    public Optional<TenantMembershipId> resolveActiveMembership(TenantId tenantId, UUID userId) {
        return tenancyAclService.resolveActiveMembership(tenantId, userId);
    }
}
