package com.andeva.atelier.platform.iam.application.acl;

import com.andeva.atelier.platform.iam.application.queryservices.BranchQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.MembershipQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.UserQueryService;
import com.andeva.atelier.platform.iam.domain.model.enums.MembershipStatus;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipByTenantAndUserQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.BranchAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.BranchGeofenceAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.TenantAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.UserAclDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound Open Host Service (OHS) implementation providing tenancy ACL translation.
 * Translates domain entities and query results into immutable ACL DTOs for consuming contexts.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class TenancyContextFacadeImpl implements TenancyContextFacade {

    private final TenantQueryService tenantQueryService;
    private final BranchQueryService branchQueryService;
    private final UserQueryService userQueryService;
    private final MembershipQueryService membershipQueryService;

    public TenancyContextFacadeImpl(
            TenantQueryService tenantQueryService,
            BranchQueryService branchQueryService,
            UserQueryService userQueryService,
            MembershipQueryService membershipQueryService
    ) {
        this.tenantQueryService = Objects.requireNonNull(tenantQueryService, "TenantQueryService cannot be null");
        this.branchQueryService = Objects.requireNonNull(branchQueryService, "BranchQueryService cannot be null");
        this.userQueryService = Objects.requireNonNull(userQueryService, "UserQueryService cannot be null");
        this.membershipQueryService = Objects.requireNonNull(membershipQueryService, "MembershipQueryService cannot be null");
    }

    @Override
    public Optional<TenantAclDto> fetchTenantById(UUID tenantId) {
        if (tenantId == null) {
            return Optional.empty();
        }
        return tenantQueryService.handle(new GetTenantByIdQuery(TenantId.of(tenantId)))
                .map(tenant -> new TenantAclDto(
                        tenant.id().value(),
                        tenant.name(),
                        tenant.legalName(),
                        tenant.taxId().value(),
                        tenant.status().name()
                ));
    }

    @Override
    public Optional<BranchAclDto> fetchBranchById(UUID branchId) {
        if (branchId == null) {
            return Optional.empty();
        }
        return branchQueryService.handle(new GetBranchByIdQuery(BranchId.of(branchId)))
                .map(branch -> new BranchAclDto(
                        branch.id().value(),
                        branch.tenantId().value(),
                        branch.name(),
                        branch.sunatCode(),
                        branch.isActive()
                ));
    }

    @Override
    public Optional<UserAclDto> fetchUserById(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userQueryService.handle(new GetUserByIdQuery(UserId.of(userId)))
                .map(user -> new UserAclDto(
                        user.id().value(),
                        user.email().value(),
                        user.profile().getFullName(),
                        user.status().name()
                ));
    }

    @Override
    public boolean isUserStaffMemberOfTenant(UUID userId, UUID tenantId) {
        if (userId == null || tenantId == null) {
            return false;
        }
        return membershipQueryService.handle(new GetMembershipByTenantAndUserQuery(TenantId.of(tenantId), UserId.of(userId)))
                .map(membership -> membership.status() == MembershipStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    public boolean isPointWithinBranchGeofence(UUID branchId, double latitude, double longitude) {
        if (branchId == null) {
            return false;
        }
        return branchQueryService.handle(new GetBranchByIdQuery(BranchId.of(branchId)))
                .map(branch -> branch.isWithinGeofence(GeoPoint.of(latitude, longitude)))
                .orElse(false);
    }

    @Override
    public Optional<BranchGeofenceAclDto> fetchBranchGeofence(UUID branchId) {
        if (branchId == null) {
            return Optional.empty();
        }
        return branchQueryService.handle(new GetBranchByIdQuery(BranchId.of(branchId)))
                .map(branch -> new BranchGeofenceAclDto(
                        branch.id().value(),
                        branch.location().latitude(),
                        branch.location().longitude(),
                        branch.geofenceRadiusMeters()
                ));
    }
}
