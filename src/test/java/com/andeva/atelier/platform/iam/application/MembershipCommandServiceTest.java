package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.commandservices.MembershipCommandService;
import com.andeva.atelier.platform.iam.application.internal.commandservices.MembershipCommandServiceImpl;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.commands.AssignRolesToMembershipCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.DeactivateMembershipCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateMembershipCompensationCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.MembershipStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
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

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link MembershipCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Membership Command Service Unit Tests")
class MembershipCommandServiceTest {

    @Mock
    private TenantMembershipRepository membershipRepository;
    @Mock
    private RoleRepository roleRepository;

    private MembershipCommandService membershipCommandService;

    private final TenantId tenantId = TenantId.generate();
    private final UserId userId = UserId.generate();

    @BeforeEach
    void setUp() {
        membershipCommandService = new MembershipCommandServiceImpl(
                membershipRepository,
                roleRepository
        );
    }

    @Test
    @DisplayName("Should assign new roles to membership successfully")
    void shouldAssignRolesSuccessfully() {
        Role initialRole = Role.createCustom(tenantId, "Mechanic", "Workshop mechanic", Set.of(Permission.create("mro:exec", "Exec", "OPS")));
        Role newRole = Role.createCustom(tenantId, "Lead Tech", "Senior technician", Set.of(Permission.create("mro:lead", "Lead", "OPS")));
        TenantMembership membership = TenantMembership.create(tenantId, userId, SalaryType.FIXED, Money.soles(2000.0), Set.of(initialRole));

        AssignRolesToMembershipCommand command = new AssignRolesToMembershipCommand(membership.id(), Set.of(newRole.id()));

        when(membershipRepository.findById(membership.id())).thenReturn(Optional.of(membership));
        when(roleRepository.findById(newRole.id())).thenReturn(Optional.of(newRole));
        when(membershipRepository.save(any(TenantMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<TenantMembership, ApplicationError> result = membershipCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        TenantMembership updated = result.getOrThrow();
        assertThat(updated.assignedRoles()).contains(newRole);
        assertThat(updated.assignedRoles()).doesNotContain(initialRole);
        verify(membershipRepository).save(membership);
    }

    @Test
    @DisplayName("Should reject role assignment when membership is not found")
    void shouldRejectWhenMembershipNotFound() {
        TenantMembershipId membershipId = TenantMembershipId.generate();
        AssignRolesToMembershipCommand command = new AssignRolesToMembershipCommand(membershipId, Set.of(RoleId.generate()));

        when(membershipRepository.findById(membershipId)).thenReturn(Optional.empty());

        Result<TenantMembership, ApplicationError> result = membershipCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("Should reject role assignment when a role belongs to another tenant")
    void shouldRejectWhenRoleBelongsToAnotherTenant() {
        Role initialRole = Role.createCustom(tenantId, "Mechanic", "Workshop mechanic", Set.of(Permission.create("mro:exec", "Exec", "OPS")));
        TenantMembership membership = TenantMembership.create(tenantId, userId, SalaryType.FIXED, Money.soles(2000.0), Set.of(initialRole));

        TenantId otherTenantId = TenantId.generate();
        Role foreignRole = Role.createCustom(otherTenantId, "Foreign Role", "From other workshop", Set.of());

        AssignRolesToMembershipCommand command = new AssignRolesToMembershipCommand(membership.id(), Set.of(foreignRole.id()));

        when(membershipRepository.findById(membership.id())).thenReturn(Optional.of(membership));
        when(roleRepository.findById(foreignRole.id())).thenReturn(Optional.of(foreignRole));

        Result<TenantMembership, ApplicationError> result = membershipCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        assertThat(result.getError().message()).contains("different workshop tenant");
    }

    @Test
    @DisplayName("Should update membership compensation scheme and base salary successfully")
    void shouldUpdateCompensationSuccessfully() {
        Role role = Role.createCustom(tenantId, "Advisor", "Advisor role", Set.of(Permission.create("crm:read", "Read", "CRM")));
        TenantMembership membership = TenantMembership.create(tenantId, userId, SalaryType.FIXED, Money.soles(1500.0), Set.of(role));

        UpdateMembershipCompensationCommand command = new UpdateMembershipCompensationCommand(
                membership.id(), SalaryType.HOURLY, Money.soles(45.0)
        );

        when(membershipRepository.findById(membership.id())).thenReturn(Optional.of(membership));
        when(membershipRepository.save(any(TenantMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<TenantMembership, ApplicationError> result = membershipCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        TenantMembership updated = result.getOrThrow();
        assertThat(updated.salaryType()).isEqualTo(SalaryType.HOURLY);
        assertThat(updated.baseSalary().amount()).isEqualByComparingTo("45.00");
    }

    @Test
    @DisplayName("Should deactivate membership successfully")
    void shouldDeactivateMembershipSuccessfully() {
        Role role = Role.createCustom(tenantId, "Advisor", "Advisor role", Set.of(Permission.create("crm:read", "Read", "CRM")));
        TenantMembership membership = TenantMembership.create(tenantId, userId, SalaryType.FIXED, Money.soles(1500.0), Set.of(role));

        DeactivateMembershipCommand command = new DeactivateMembershipCommand(membership.id());

        when(membershipRepository.findById(membership.id())).thenReturn(Optional.of(membership));
        when(membershipRepository.save(any(TenantMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Void, ApplicationError> result = membershipCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(membership.status()).isEqualTo(MembershipStatus.INACTIVE);
        verify(membershipRepository).save(membership);
    }
}
