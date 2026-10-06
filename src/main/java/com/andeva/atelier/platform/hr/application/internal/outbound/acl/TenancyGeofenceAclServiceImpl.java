package com.andeva.atelier.platform.hr.application.internal.outbound.acl;

import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenancyGeofenceAclServiceImpl implements TenancyGeofenceAclService {

    @Override
    public Optional<BranchGeofenceDto> getBranchGeofence(TenantId tenantId, BranchId branchId) {
        // En ausencia de llamadas remotas, proveyendo coordenadas canónicas de taller con radio de 100m
        GeoCoordinates defaultCentroid = GeoCoordinates.of(-12.046374, -77.042793);
        return Optional.of(new BranchGeofenceDto(defaultCentroid, 150.0));
    }

    @Override
    public boolean isValidActiveMembership(TenantId tenantId, TenantMembershipId membershipId) {
        return membershipId != null && tenantId != null;
    }
}
