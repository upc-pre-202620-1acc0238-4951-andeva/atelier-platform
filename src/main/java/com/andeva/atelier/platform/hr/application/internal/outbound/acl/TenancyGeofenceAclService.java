package com.andeva.atelier.platform.hr.application.internal.outbound.acl;

import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Optional;

public interface TenancyGeofenceAclService {
    Optional<BranchGeofenceDto> getBranchGeofence(TenantId tenantId, BranchId branchId);
    boolean isValidActiveMembership(TenantId tenantId, TenantMembershipId membershipId);

    record BranchGeofenceDto(GeoCoordinates centroid, double allowedRadiusMeters) {}
}
