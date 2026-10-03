package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.commandservices.RoleCommandService;
import com.andeva.atelier.platform.iam.application.internal.commandservices.RoleCommandServiceImpl;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateCustomRoleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.DeleteCustomRoleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ProvisionTenantRolesCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetRoleToDefaultsCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateRolePermissionsCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.repositories.PermissionRepository;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link RoleCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Role Command Service Unit Tests")
class RoleCommandServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private TenantMembershipRepository membershipRepository;

    private RoleCommandService roleCommandService;

    private final TenantId tenantId = TenantId.generate();

    @BeforeEach
    void setUp() {
        roleCommandService = new RoleCommandServiceImpl(
                roleRepository,
                permissionRepository,
                membershipRepository
        );
    }

    @Test
    @DisplayName("Should create custom role successfully")
    void shouldCreateCustomRoleSuccessfully() {
        Permission perm = Permission.create("mro:diag:create", "Create diagnostics", "OPERATIONS");
        CreateCustomRoleCommand command = new CreateCustomRoleCommand(
                tenantId, "Master Tech", "Senior diagnostician", Set.of(perm.id())
        );

        when(roleRepository.existsByTenantIdAndName(tenantId, "Master Tech")).thenReturn(false);
        when(permissionRepository.findAll()).thenReturn(List.of(perm));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Role, ApplicationError> result = roleCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Role role = result.getOrThrow();
        assertThat(role.name()).isEqualTo("Master Tech");
        assertThat(role.isSystemRole()).isFalse();
        assertThat(role.permissions()).contains(perm);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should reject custom role creation when name already exists in tenant")
    void shouldRejectWhenRoleNameAlreadyExists() {
        CreateCustomRoleCommand command = new CreateCustomRoleCommand(
                tenantId, "Master Tech", "Senior diagnostician", Set.of()
        );

        when(roleRepository.existsByTenantIdAndName(tenantId, "Master Tech")).thenReturn(true);

        Result<Role, ApplicationError> result = roleCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update role permissions sovereignly")
    void shouldUpdateRolePermissionsSuccessfully() {
        Permission perm1 = Permission.create("crm:read", "Read customers", "CRM");
        Permission perm2 = Permission.create("crm:write", "Write customers", "CRM");
        Role role = Role.createCustom(tenantId, "Advisor", "Customer advisor", Set.of(perm1));

        UpdateRolePermissionsCommand command = new UpdateRolePermissionsCommand(role.id(), Set.of(perm2.id()));

        when(roleRepository.findById(role.id())).thenReturn(Optional.of(role));
        when(permissionRepository.findAll()).thenReturn(List.of(perm1, perm2));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Role, ApplicationError> result = roleCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Role updated = result.getOrThrow();
        assertThat(updated.permissions()).contains(perm2);
        assertThat(updated.permissions()).doesNotContain(perm1);
    }

    @Test
    @DisplayName("Should reset system role to template defaults")
    void shouldResetSystemRoleToDefaultsSuccessfully() {
        Permission perm = Permission.create("all:access", "Full Access", "SYSTEM");
        Role systemRole = Role.fromTemplate(tenantId, RoleTemplate.ROLE_WORKSHOP_ADMIN, Set.of());
        ResetRoleToDefaultsCommand command = new ResetRoleToDefaultsCommand(systemRole.id());

        when(roleRepository.findById(systemRole.id())).thenReturn(Optional.of(systemRole));
        when(permissionRepository.findAll()).thenReturn(List.of(perm));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Role, ApplicationError> result = roleCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Role reset = result.getOrThrow();
        assertThat(reset.permissions()).contains(perm);
    }

    @Test
    @DisplayName("Should reject resetting custom role to template defaults")
    void shouldRejectResettingCustomRole() {
        Role customRole = Role.createCustom(tenantId, "Custom", "Description", Set.of());
        ResetRoleToDefaultsCommand command = new ResetRoleToDefaultsCommand(customRole.id());

        when(roleRepository.findById(customRole.id())).thenReturn(Optional.of(customRole));

        Result<Role, ApplicationError> result = roleCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("Should delete unassigned custom role successfully")
    void shouldDeleteUnassignedCustomRoleSuccessfully() {
        Role customRole = Role.createCustom(tenantId, "Temporary", "Temp Role", Set.of());
        DeleteCustomRoleCommand command = new DeleteCustomRoleCommand(customRole.id());

        when(roleRepository.findById(customRole.id())).thenReturn(Optional.of(customRole));
        when(membershipRepository.findByTenantId(tenantId)).thenReturn(List.of());

        Result<Void, ApplicationError> result = roleCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        verify(roleRepository).delete(customRole);
    }

    @Test
    @DisplayName("Should reject deleting protected system role")
    void shouldRejectDeletingSystemRole() {
        Role systemRole = Role.fromTemplate(tenantId, RoleTemplate.ROLE_WORKSHOP_ADMIN, Set.of());
        DeleteCustomRoleCommand command = new DeleteCustomRoleCommand(systemRole.id());

        when(roleRepository.findById(systemRole.id())).thenReturn(Optional.of(systemRole));

        Result<Void, ApplicationError> result = roleCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should reject deleting role when assigned to active staff membership")
    void shouldRejectDeletingRoleInUse() {
        Role customRole = Role.createCustom(tenantId, "Mechanic", "Mechanic role", Set.of());
        TenantMembership membership = TenantMembership.create(
                tenantId, UserId.generate(), SalaryType.FIXED, Money.soles(1200.0), Set.of(customRole)
        );
        DeleteCustomRoleCommand command = new DeleteCustomRoleCommand(customRole.id());

        when(roleRepository.findById(customRole.id())).thenReturn(Optional.of(customRole));
        when(membershipRepository.findByTenantId(tenantId)).thenReturn(List.of(membership));

        Result<Void, ApplicationError> result = roleCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should provision all canonical roles for new tenant")
    void shouldProvisionAllRolesForTenant() {
        ProvisionTenantRolesCommand command = new ProvisionTenantRolesCommand(tenantId);
        when(permissionRepository.findAll()).thenReturn(List.of());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<List<Role>, ApplicationError> result = roleCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        List<Role> provisioned = result.getOrThrow();
        assertThat(provisioned).hasSize(RoleTemplate.values().length);
    }
}
