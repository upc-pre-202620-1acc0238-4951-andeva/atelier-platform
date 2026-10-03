package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.internal.queryservices.*;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.iam.domain.model.queries.*;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.domain.repositories.*;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for IAM Query Services.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IAM Query Services Unit Tests")
class QueryServicesTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private TenantMembershipRepository membershipRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;

    private TenantQueryServiceImpl tenantQueryService;
    private UserQueryServiceImpl userQueryService;
    private BranchQueryServiceImpl branchQueryService;
    private MembershipQueryServiceImpl membershipQueryService;
    private RoleQueryServiceImpl roleQueryService;

    private final TenantId tenantId = TenantId.generate();
    private final UserId userId = UserId.generate();
    private final TaxId taxId = TaxId.ruc("20456789014");
    private final EmailAddress email = EmailAddress.of("staff@atelier.pe");

    @BeforeEach
    void setUp() {
        tenantQueryService = new TenantQueryServiceImpl(tenantRepository);
        userQueryService = new UserQueryServiceImpl(userRepository);
        branchQueryService = new BranchQueryServiceImpl(branchRepository);
        membershipQueryService = new MembershipQueryServiceImpl(membershipRepository);
        roleQueryService = new RoleQueryServiceImpl(roleRepository, permissionRepository);
    }

    @Test
    @DisplayName("TenantQueryService: should resolve tenant by ID and Tax ID")
    void shouldResolveTenantQueries() {
        Tenant tenant = Tenant.create("AutoTaller", "AutoTaller S.A.", taxId);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.findByTaxId(taxId)).thenReturn(Optional.of(tenant));

        assertThat(tenantQueryService.handle(new GetTenantByIdQuery(tenantId))).contains(tenant);
        assertThat(tenantQueryService.handle(new GetTenantByTaxIdQuery(taxId))).contains(tenant);
    }

    @Test
    @DisplayName("UserQueryService: should resolve user by ID and email")
    void shouldResolveUserQueries() {
        User user = User.registerWithLocalCredentials(
                email,
                Password.of("$2a$12$abcdefghijklmnopqrstuvwxABCDEFGHIJKLMNOPQRSTUVWXYZ012"),
                PersonName.of("Carlos", "García"),
                PhoneNumber.of("+51987654321")
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThat(userQueryService.handle(new GetUserByIdQuery(userId))).contains(user);
        assertThat(userQueryService.handle(new GetUserByEmailQuery(email))).contains(user);
    }

    @Test
    @DisplayName("BranchQueryService: should resolve branch by ID and list by Tenant")
    void shouldResolveBranchQueries() {
        Branch branch = Branch.create(tenantId, "Sede Central", "0000", GeoPoint.of(-12.0, -77.0), 300);
        when(branchRepository.findById(branch.id())).thenReturn(Optional.of(branch));
        when(branchRepository.findByTenantId(tenantId)).thenReturn(List.of(branch));

        assertThat(branchQueryService.handle(new GetBranchByIdQuery(branch.id()))).contains(branch);
        assertThat(branchQueryService.handle(new GetBranchesByTenantIdQuery(tenantId))).containsExactly(branch);
    }

    @Test
    @DisplayName("MembershipQueryService: should resolve membership by ID, tenant, and user")
    void shouldResolveMembershipQueries() {
        Role role = Role.createCustom(tenantId, "Admin", "Admin Role", Set.of());
        TenantMembership membership = TenantMembership.create(
                tenantId, userId, SalaryType.FIXED, Money.soles(1000.0), Set.of(role)
        );
        when(membershipRepository.findById(membership.id())).thenReturn(Optional.of(membership));
        when(membershipRepository.findByTenantId(tenantId)).thenReturn(List.of(membership));
        when(membershipRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(membership));

        assertThat(membershipQueryService.handle(new GetMembershipByIdQuery(membership.id()))).contains(membership);
        assertThat(membershipQueryService.handle(new GetMembershipsByTenantIdQuery(tenantId))).containsExactly(membership);
        assertThat(membershipQueryService.handle(new GetMembershipByTenantAndUserQuery(tenantId, userId))).contains(membership);
    }

    @Test
    @DisplayName("RoleQueryService: should resolve role by ID, list by tenant, and list all permissions")
    void shouldResolveRoleQueries() {
        Role role = Role.createCustom(tenantId, "Admin", "Admin Role", Set.of());
        Permission perm = Permission.create("mro:read", "Read", "OPS");
        when(roleRepository.findById(role.id())).thenReturn(Optional.of(role));
        when(roleRepository.findByTenantId(tenantId)).thenReturn(List.of(role));
        when(permissionRepository.findAll()).thenReturn(List.of(perm));

        assertThat(roleQueryService.handle(new GetRoleByIdQuery(role.id()))).contains(role);
        assertThat(roleQueryService.handle(new GetRolesByTenantIdQuery(tenantId))).containsExactly(role);
        assertThat(roleQueryService.handle(new GetAllPermissionsQuery())).containsExactly(perm);
    }
}
