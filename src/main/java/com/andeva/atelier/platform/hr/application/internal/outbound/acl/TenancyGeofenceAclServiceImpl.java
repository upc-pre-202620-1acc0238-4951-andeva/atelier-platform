package com.andeva.atelier.platform.hr.application.internal.outbound.acl;

import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Joel Huamani Estefanero
 */
@Service
public class TenancyGeofenceAclServiceImpl implements TenancyGeofenceAclService {

    private final TenancyContextFacade tenancyContextFacade;

    @Autowired
    public TenancyGeofenceAclServiceImpl(@Autowired(required = false) TenancyContextFacade tenancyContextFacade) {
        this.tenancyContextFacade = tenancyContextFacade;
    }

    public TenancyGeofenceAclServiceImpl() {
        this(null);
    }

    @Override
    public Optional<BranchGeofenceDto> getBranchGeofence(TenantId tenantId, BranchId branchId) {
        if (this.tenancyContextFacade != null && branchId != null) {
            try {
                var dtoOpt = this.tenancyContextFacade.fetchBranchGeofence(branchId.value());
                if (dtoOpt.isPresent()) {
                    var dto = dtoOpt.get();
                    return Optional.of(new BranchGeofenceDto(GeoCoordinates.of(dto.latitude(), dto.longitude()), dto.radiusMeters()));
                }
            } catch (Exception e) {
                // fallback
            }
        }
        GeoCoordinates defaultCentroid = GeoCoordinates.of(-12.046374, -77.042793);
        return Optional.of(new BranchGeofenceDto(defaultCentroid, 150.0));
    }

    @Override
    public boolean isValidActiveMembership(TenantId tenantId, TenantMembershipId membershipId) {
        if (tenantId == null || membershipId == null) return false;
        if (this.tenancyContextFacade != null) {
            try {
                return this.tenancyContextFacade.isUserStaffMemberOfTenant(membershipId.value(), tenantId.value());
            } catch (Exception e) {
                // fallback
            }
        }
        return true;
    }
}
