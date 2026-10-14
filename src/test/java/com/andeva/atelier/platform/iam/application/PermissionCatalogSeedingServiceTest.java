package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.internal.commandservices.PermissionCatalogSeedingService;
import com.andeva.atelier.platform.iam.application.internal.commandservices.PermissionCatalogSeedingService.SeedingResult;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PermissionCatalog;
import com.andeva.atelier.platform.iam.domain.repositories.PermissionRepository;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for {@link PermissionCatalogSeedingService}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Permission Catalog Seeding Service Unit Tests")
class PermissionCatalogSeedingServiceTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TenantMembershipRepository membershipRepository;

    private PermissionCatalogSeedingService seedingService;

    private final TenantId tenantId = TenantId.generate();

    @BeforeEach
    void setUp() {
        seedingService = new PermissionCatalogSeedingService(permissionRepository, roleRepository, membershipRepository);
    }

    @Test
    @DisplayName("Should seed the catalog and migrate empty factory roles of existing tenants to their subsets")
    void shouldSeedCatalogAndMigrateExistingTenant() {
        List<Role> legacyRoles = new ArrayList<>();
        for (RoleTemplate template : RoleTemplate.values()) {
            if (template != RoleTemplate.ROLE_INVENTORY_MANAGER) {
                legacyRoles.add(Role.fromTemplate(tenantId, template, List.of()));
            }
        }
        Role legacyAdmin = legacyRoles.stream()
                .filter(r -> RoleTemplate.ROLE_WORKSHOP_ADMIN.code().equals(r.code())).findFirst().orElseThrow();
        TenantMembership registrant = TenantMembership.create(
                tenantId, UserId.generate(), SalaryType.FIXED, Money.soles(0.0), Set.of(legacyAdmin));

        when(permissionRepository.findAll()).thenReturn(List.of());
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findAllSystemRoles()).thenReturn(legacyRoles);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.findByTenantId(tenantId)).thenReturn(List.of(registrant));
        when(membershipRepository.findEarliestByTenantId(tenantId)).thenReturn(Optional.of(registrant));

        SeedingResult result = seedingService.synchronize();

        assertThat(result.permissionsCreated()).isEqualTo(PermissionCatalog.entries().size());
        assertThat(result.rolesUpdated()).isEqualTo(legacyRoles.size());
        assertThat(result.rolesProvisioned()).isEqualTo(1);
        assertThat(result.ownersAssigned()).isEqualTo(1);

        for (Role role : legacyRoles) {
            RoleTemplate template = RoleTemplate.fromCode(role.code()).orElseThrow();
            assertThat(permissionNames(role)).containsExactlyInAnyOrderElementsOf(template.defaultPermissionNames());
        }

        ArgumentCaptor<Role> saved = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, atLeastOnce()).save(saved.capture());
        Role provisionedInventory = saved.getAllValues().stream()
                .filter(r -> RoleTemplate.ROLE_INVENTORY_MANAGER.code().equals(r.code())).findFirst().orElseThrow();
        assertThat(permissionNames(provisionedInventory))
                .containsExactlyInAnyOrderElementsOf(RoleTemplate.ROLE_INVENTORY_MANAGER.defaultPermissionNames());

        assertThat(registrant.assignedRoles()).extracting(Role::code)
                .containsExactlyInAnyOrder("ROLE_WORKSHOP_ADMIN", "ROLE_WORKSHOP_OWNER");
        verify(membershipRepository).save(registrant);
    }

    @Test
    @DisplayName("Should perform no writes when catalog and tenant roles are already synchronized")
    void shouldBeIdempotent() {
        List<Permission> catalog = PermissionCatalog.entries().stream()
                .map(entry -> Permission.create(entry.name(), entry.description(), entry.category()))
                .toList();
        List<Role> roles = new ArrayList<>();
        for (RoleTemplate template : RoleTemplate.values()) {
            roles.add(Role.fromTemplate(tenantId, template, catalog));
        }
        Role owner = roles.get(0);
        TenantMembership ownerMembership = TenantMembership.create(
                tenantId, UserId.generate(), SalaryType.FIXED, Money.soles(0.0), Set.of(owner));

        when(permissionRepository.findAll()).thenReturn(catalog);
        when(roleRepository.findAllSystemRoles()).thenReturn(roles);
        when(membershipRepository.findByTenantId(tenantId)).thenReturn(List.of(ownerMembership));

        SeedingResult result = seedingService.synchronize();

        assertThat(result).isEqualTo(new SeedingResult(0, 0, 0, 0));
        verify(permissionRepository, never()).save(any());
        verify(roleRepository, never()).save(any());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should grant only newly introduced permissions and keep sovereign edits of existing roles")
    void shouldGrantOnlyNewPermissions() {
        List<Permission> catalog = PermissionCatalog.entries().stream()
                .filter(entry -> !entry.name().equals("operations:tasks:complete"))
                .map(entry -> Permission.create(entry.name(), entry.description(), entry.category()))
                .collect(Collectors.toCollection(ArrayList::new));
        Role mechanic = Role.fromTemplate(tenantId, RoleTemplate.ROLE_MECHANIC, catalog);
        mechanic.revokePermission(permission(catalog, "iot:telemetry:ingest").id());
        Role owner = Role.fromTemplate(tenantId, RoleTemplate.ROLE_WORKSHOP_OWNER, catalog);
        List<Role> roles = new ArrayList<>(List.of(owner, mechanic));
        for (RoleTemplate template : List.of(RoleTemplate.ROLE_WORKSHOP_ADMIN,
                RoleTemplate.ROLE_SERVICE_ADVISOR, RoleTemplate.ROLE_INVENTORY_MANAGER)) {
            roles.add(Role.fromTemplate(tenantId, template, catalog));
        }
        TenantMembership ownerMembership = TenantMembership.create(
                tenantId, UserId.generate(), SalaryType.FIXED, Money.soles(0.0), Set.of(owner));

        when(permissionRepository.findAll()).thenReturn(catalog);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findAllSystemRoles()).thenReturn(roles);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.findByTenantId(tenantId)).thenReturn(List.of(ownerMembership));

        SeedingResult result = seedingService.synchronize();

        assertThat(result).isEqualTo(new SeedingResult(1, 1, 0, 0));
        assertThat(permissionNames(mechanic))
                .contains("operations:tasks:complete")
                .doesNotContain("iot:telemetry:ingest");
    }

    private static Permission permission(List<Permission> catalog, String name) {
        return catalog.stream().filter(p -> p.name().equals(name)).findFirst().orElseThrow();
    }

    private static Set<String> permissionNames(Role role) {
        return role.permissions().stream().map(Permission::name).collect(Collectors.toSet());
    }
}
