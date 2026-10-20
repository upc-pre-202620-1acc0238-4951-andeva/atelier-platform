package com.andeva.atelier.platform.hr.application.queryservices;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;

import java.util.Optional;
import java.util.UUID;

/**
 * Resolves the authenticated user to the staff membership that attendance and payroll records belong to.
 */
public interface StaffMembershipQueryService {

    Optional<TenantMembershipId> resolveActiveMembership(TenantId tenantId, UUID userId);
}
