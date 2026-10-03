package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.acl.TenancyContextFacadeImpl;
import com.andeva.atelier.platform.iam.application.queryservices.BranchQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.MembershipQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.UserQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipByTenantAndUserQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.BranchAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.BranchGeofenceAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.TenantAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.UserAclDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for {@link TenancyContextFacadeImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tenancy Context Facade Unit Tests")
class TenancyContextFacadeTest {

    @Mock
    private TenantQueryService tenantQueryService;
    @Mock
    private BranchQueryService branchQueryService;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private MembershipQueryService membershipQueryService;

    private TenancyContextFacade tenancyContextFacade;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID branchUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        tenancyContextFacade = new TenancyContextFacadeImpl(
                tenantQueryService,
                branchQueryService,
                userQueryService,
                membershipQueryService
        );
    }

    @Test
    @DisplayName("Should fetch and map TenantAclDto successfully")
    void shouldFetchTenantByIdSuccessfully() {
        Tenant tenant = Tenant.create("AutoTaller", "AutoTaller S.A.", TaxId.ruc("20456789014"));
        when(tenantQueryService.handle(any(GetTenantByIdQuery.class))).thenReturn(Optional.of(tenant));

        Optional<TenantAclDto> result = tenancyContextFacade.fetchTenantById(tenantUuid);

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("AutoTaller");
        assertThat(result.get().taxId()).isEqualTo("20456789014");
    }

    @Test
    @DisplayName("Should fetch and map BranchAclDto successfully")
    void shouldFetchBranchByIdSuccessfully() {
        Branch branch = Branch.create(TenantId.of(tenantUuid), "Sede Principal", "0000", GeoPoint.of(-12.0, -77.0), 500);
        when(branchQueryService.handle(any(GetBranchByIdQuery.class))).thenReturn(Optional.of(branch));

        Optional<BranchAclDto> result = tenancyContextFacade.fetchBranchById(branchUuid);

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Sede Principal");
        assertThat(result.get().sunatCode()).isEqualTo("0000");
    }

    @Test
    @DisplayName("Should fetch and map UserAclDto successfully")
    void shouldFetchUserByIdSuccessfully() {
        User user = User.registerWithLocalCredentials(
                EmailAddress.of("carlos@atelier.pe"),
                Password.of("$2a$12$abcdefghijklmnopqrstuvwxABCDEFGHIJKLMNOPQRSTUVWXYZ012"),
                PersonName.of("Carlos", "García"),
                PhoneNumber.of("+51987654321")
        );
        when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.of(user));

        Optional<UserAclDto> result = tenancyContextFacade.fetchUserById(userUuid);

        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo("carlos@atelier.pe");
        assertThat(result.get().fullName()).isEqualTo("Carlos García");
    }

    @Test
    @DisplayName("Should verify whether user is active staff member of tenant")
    void shouldVerifyStaffMembership() {
        Role role = Role.createCustom(TenantId.of(tenantUuid), "Admin", "Admin", Set.of());
        TenantMembership membership = TenantMembership.create(
                TenantId.of(tenantUuid), UserId.of(userUuid), SalaryType.FIXED, Money.soles(1000.0), Set.of(role)
        );
        when(membershipQueryService.handle(any(GetMembershipByTenantAndUserQuery.class))).thenReturn(Optional.of(membership));

        boolean isStaff = tenancyContextFacade.isUserStaffMemberOfTenant(userUuid, tenantUuid);

        assertThat(isStaff).isTrue();
    }

    @Test
    @DisplayName("Should evaluate whether geographic point is within branch geofence")
    void shouldEvaluateGeofence() {
        Branch branch = Branch.create(TenantId.of(tenantUuid), "Sede Central", "0000", GeoPoint.of(-12.046374, -77.042793), 500);
        when(branchQueryService.handle(any(GetBranchByIdQuery.class))).thenReturn(Optional.of(branch));

        // Coordinate very close (~50m)
        boolean inside = tenancyContextFacade.isPointWithinBranchGeofence(branchUuid, -12.0465, -77.0428);
        assertThat(inside).isTrue();

        // Coordinate far away (~5km)
        boolean outside = tenancyContextFacade.isPointWithinBranchGeofence(branchUuid, -12.1000, -77.0000);
        assertThat(outside).isFalse();
    }

    @Test
    @DisplayName("Should fetch branch geofence parameters")
    void shouldFetchBranchGeofence() {
        Branch branch = Branch.create(TenantId.of(tenantUuid), "Sede Central", "0000", GeoPoint.of(-12.046374, -77.042793), 500);
        when(branchQueryService.handle(any(GetBranchByIdQuery.class))).thenReturn(Optional.of(branch));

        Optional<BranchGeofenceAclDto> result = tenancyContextFacade.fetchBranchGeofence(branchUuid);

        assertThat(result).isPresent();
        assertThat(result.get().radiusMeters()).isEqualTo(500);
        assertThat(result.get().latitude()).isEqualTo(-12.046374);
        assertThat(result.get().longitude()).isEqualTo(-77.042793);
    }
}
