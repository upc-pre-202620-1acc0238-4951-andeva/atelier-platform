package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.internal.commandservices.PermissionCatalogSeedingService;
import com.andeva.atelier.platform.iam.application.internal.commandservices.PermissionCatalogSeedingService.SeedingResult;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
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
    @DisplayName("Should migrate a legacy tenant to the eight factory roles keeping role ids and memberships")
    void shouldMigrateLegacyTenant() {
        Permission obsolete = Permission.create("operations:work-orders:write", "Legacy permission", "OPERATIONS");
        Role legacyOwner = legacySystemRole("ROLE_WORKSHOP_OWNER", "Workshop Owner", obsolete);
        Role legacyAdmin = legacySystemRole("ROLE_WORKSHOP_ADMIN", "Workshop Administrator", obsolete);
        Role legacyAdvisor = legacySystemRole("ROLE_SERVICE_ADVISOR", "Service Advisor", obsolete);
        Role legacyMechanic = legacySystemRole("ROLE_MECHANIC", "Mechanic", obsolete);
        Role legacyInventory = legacySystemRole("ROLE_INVENTORY_MANAGER", "Inventory Manager", obsolete);
        Role custom = Role.createCustom(tenantId, "Lavador", "Custom role", Set.of(obsolete));
        List<Role> legacyRoles = List.of(legacyOwner, legacyAdmin, legacyAdvisor, legacyMechanic, legacyInventory, custom);
        RoleId adminId = legacyAdmin.id();
        TenantMembership registrant = TenantMembership.create(
                tenantId, UserId.generate(), SalaryType.FIXED, Money.soles(0.0), Set.of(legacyAdmin));

        when(permissionRepository.findAll()).thenReturn(List.of(obsolete));
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findAll()).thenReturn(legacyRoles);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.findByTenantId(tenantId)).thenReturn(List.of(registrant));
        when(membershipRepository.findEarliestByTenantId(tenantId)).thenReturn(Optional.of(registrant));

        SeedingResult result = seedingService.synchronize();

        assertThat(result).isEqualTo(new SeedingResult(PermissionCatalog.entries().size(), 1, 6, 3, 1));
        verify(permissionRepository).delete(obsolete);

        assertThat(legacyAdmin.id()).isEqualTo(adminId);
        assertThat(legacyAdmin.code()).isEqualTo("ROLE_WORKSHOP_ADMINISTRATOR");
        assertThat(legacyAdmin.name()).isEqualTo("Administrador de Taller");
        assertThat(legacyMechanic.name()).isEqualTo("Técnico Mecánico");
        for (Role role : List.of(legacyOwner, legacyAdmin, legacyAdvisor, legacyMechanic, legacyInventory)) {
            RoleTemplate template = RoleTemplate.fromCode(role.code()).orElseThrow();
            assertThat(permissionNames(role)).containsExactlyInAnyOrderElementsOf(template.defaultPermissionNames());
        }
        assertThat(custom.permissions()).isEmpty();

        ArgumentCaptor<Role> saved = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, atLeastOnce()).save(saved.capture());
        Set<String> provisioned = saved.getAllValues().stream()
                .filter(role -> !legacyRoles.contains(role))
                .map(Role::code)
                .collect(Collectors.toSet());
        assertThat(provisioned).containsExactlyInAnyOrder("ROLE_CHIEF_MECHANIC", "ROLE_RECEPTIONIST", "ROLE_CASHIER");

        assertThat(registrant.assignedRoles()).extracting(Role::code)
                .containsExactlyInAnyOrder("ROLE_WORKSHOP_ADMINISTRATOR", "ROLE_WORKSHOP_OWNER");
        verify(membershipRepository).save(registrant);
    }

    @Test
    @DisplayName("Should perform no writes when catalog and tenant roles are already synchronized")
    void shouldBeIdempotent() {
        List<Permission> catalog = seededCatalog();
        List<Role> roles = new ArrayList<>();
        for (RoleTemplate template : RoleTemplate.values()) {
            roles.add(Role.fromTemplate(tenantId, template, catalog));
        }
        TenantMembership ownerMembership = TenantMembership.create(
                tenantId, UserId.generate(), SalaryType.FIXED, Money.soles(0.0), Set.of(roles.get(0)));

        when(permissionRepository.findAll()).thenReturn(catalog);
        when(roleRepository.findAll()).thenReturn(roles);
        when(membershipRepository.findByTenantId(tenantId)).thenReturn(List.of(ownerMembership));

        SeedingResult result = seedingService.synchronize();

        assertThat(result.hasChanges()).isFalse();
        verify(permissionRepository, never()).save(any());
        verify(permissionRepository, never()).delete(any());
        verify(roleRepository, never()).save(any());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should grant only newly introduced permissions and keep sovereign edits of existing roles")
    void shouldGrantOnlyNewPermissions() {
        List<Permission> catalog = seededCatalog().stream()
                .filter(permission -> !permission.name().equals("operations:tasks:complete"))
                .collect(Collectors.toCollection(ArrayList::new));
        List<Role> roles = new ArrayList<>();
        for (RoleTemplate template : RoleTemplate.values()) {
            roles.add(Role.fromTemplate(tenantId, template, catalog));
        }
        Role mechanic = roles.stream().filter(r -> r.code().equals("ROLE_MECHANIC")).findFirst().orElseThrow();
        mechanic.revokePermission(catalog.stream()
                .filter(p -> p.name().equals("iot:telemetry:ingest")).findFirst().orElseThrow().id());
        TenantMembership ownerMembership = TenantMembership.create(
                tenantId, UserId.generate(), SalaryType.FIXED, Money.soles(0.0), Set.of(roles.get(0)));

        when(permissionRepository.findAll()).thenReturn(catalog);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findAll()).thenReturn(roles);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.findByTenantId(tenantId)).thenReturn(List.of(ownerMembership));

        SeedingResult result = seedingService.synchronize();

        // operations:tasks:complete belongs to the chief mechanic and mechanic templates
        assertThat(result).isEqualTo(new SeedingResult(1, 0, 2, 0, 0));
        assertThat(permissionNames(mechanic))
                .contains("operations:tasks:complete")
                .doesNotContain("iot:telemetry:ingest");
    }

    private Role legacySystemRole(String code, String name, Permission... permissions) {
        return new Role(RoleId.generate(), tenantId, code, name, "Legacy factory role", true, Set.of(permissions));
    }

    private static List<Permission> seededCatalog() {
        return PermissionCatalog.entries().stream()
                .map(entry -> Permission.create(entry.name(), entry.description(), entry.category()))
                .toList();
    }

    private static Set<String> permissionNames(Role role) {
        return role.permissions().stream().map(Permission::name).collect(Collectors.toSet());
    }
}
