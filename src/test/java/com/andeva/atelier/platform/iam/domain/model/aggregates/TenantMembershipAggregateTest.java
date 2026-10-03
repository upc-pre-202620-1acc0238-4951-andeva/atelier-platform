package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.MembershipStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.events.RoleAssignedToMembershipEvent;
import com.andeva.atelier.platform.iam.domain.model.events.RoleRevokedFromMembershipEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantMembershipActivatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantMembershipCreatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantMembershipDeactivatedEvent;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for {@link TenantMembership} aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("TenantMembership Aggregate Root Unit Tests")
class TenantMembershipAggregateTest {

    private final TenantId tenantId = TenantId.generate();
    private final UserId userId = UserId.generate();
    private Role mechanicRole;
    private Role advisorRole;
    private Permission workOrdersCreate;

    @BeforeEach
    void setUp() {
        workOrdersCreate = Permission.of("mro:work-orders:create", "Create work orders", "OPERATIONS");
        Permission workOrdersRead = Permission.of("mro:work-orders:read", "Read work orders", "OPERATIONS");

        mechanicRole = Role.provisionFromTemplate(tenantId, RoleTemplate.ROLE_MECHANIC, Set.of(workOrdersCreate));
        advisorRole = Role.provisionFromTemplate(tenantId, RoleTemplate.ROLE_SERVICE_ADVISOR, Set.of(workOrdersRead));
    }

    @Test
    @DisplayName("Should create active membership with initial role and register TenantMembershipCreatedEvent")
    void shouldCreateMembership() {
        Money salary = Money.soles(2500.00);
        TenantMembership membership = TenantMembership.create(
                tenantId,
                userId,
                SalaryType.FIXED,
                salary,
                Set.of(mechanicRole)
        );

        assertThat(membership.id()).isNotNull();
        assertThat(membership.tenantId()).isEqualTo(tenantId);
        assertThat(membership.userId()).isEqualTo(userId);
        assertThat(membership.status()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(membership.salaryType()).isEqualTo(SalaryType.FIXED);
        assertThat(membership.baseSalary()).isEqualTo(salary);
        assertThat(membership.assignedRoles()).containsExactly(mechanicRole);

        assertThat(membership.domainEvents()).hasSize(1);
        assertThat(membership.domainEvents().iterator().next())
                .isInstanceOf(TenantMembershipCreatedEvent.class);
    }

    @Test
    @DisplayName("Should require at least one assigned role when creating active membership")
    void shouldRequireAtLeastOneRole() {
        Money salary = Money.soles(1500.00);
        assertThatThrownBy(() -> TenantMembership.create(tenantId, userId, SalaryType.FIXED, salary, Set.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be assigned at least one security role");
    }

    @Test
    @DisplayName("Should assign and revoke roles while enforcing at least one role remains")
    void shouldManageAssignedRoles() {
        Money salary = Money.soles(2000.00);
        TenantMembership membership = TenantMembership.create(
                tenantId,
                userId,
                SalaryType.FIXED,
                salary,
                Set.of(mechanicRole)
        );
        membership.clearDomainEvents();

        // Assign second role
        membership.assignRole(advisorRole);
        assertThat(membership.assignedRoles()).hasSize(2);
        assertThat(membership.domainEvents()).hasSize(1);
        assertThat(membership.domainEvents().iterator().next())
                .isInstanceOf(RoleAssignedToMembershipEvent.class);

        // Revoke one role
        membership.clearDomainEvents();
        membership.revokeRole(mechanicRole.id());
        assertThat(membership.assignedRoles()).containsExactly(advisorRole);
        assertThat(membership.domainEvents()).hasSize(1);
        assertThat(membership.domainEvents().iterator().next())
                .isInstanceOf(RoleRevokedFromMembershipEvent.class);

        // Attempting to revoke the last remaining role must fail
        assertThatThrownBy(() -> membership.revokeRole(advisorRole.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must retain at least one security role");
    }

    @Test
    @DisplayName("Should evaluate permissions recursively through assigned roles")
    void shouldEvaluatePermissions() {
        Money salary = Money.soles(2000.00);
        TenantMembership membership = TenantMembership.create(
                tenantId,
                userId,
                SalaryType.FIXED,
                salary,
                Set.of(mechanicRole)
        );

        assertThat(membership.hasPermission("mro:work-orders:create")).isTrue();
        assertThat(membership.hasPermission("billing:invoices:create")).isFalse();

        // When deactivated, hasPermission returns false
        membership.deactivate();
        assertThat(membership.hasPermission("mro:work-orders:create")).isFalse();
    }

    @Test
    @DisplayName("Should update compensation package and reject negative salary")
    void shouldUpdateCompensation() {
        Money salary = Money.soles(2000.00);
        TenantMembership membership = TenantMembership.create(
                tenantId,
                userId,
                SalaryType.FIXED,
                salary,
                Set.of(mechanicRole)
        );

        Money newSalary = Money.soles(25.00);
        membership.updateCompensation(SalaryType.HOURLY, newSalary);

        assertThat(membership.salaryType()).isEqualTo(SalaryType.HOURLY);
        assertThat(membership.baseSalary()).isEqualTo(newSalary);

        Money negativeSalary = Money.soles(-100.00);
        assertThatThrownBy(() -> membership.updateCompensation(SalaryType.FIXED, negativeSalary))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    @DisplayName("Should manage membership activation and deactivation")
    void shouldManageActivationAndDeactivation() {
        Money salary = Money.soles(2000.00);
        TenantMembership membership = TenantMembership.create(
                tenantId,
                userId,
                SalaryType.FIXED,
                salary,
                Set.of(mechanicRole)
        );
        membership.clearDomainEvents();

        membership.deactivate();
        assertThat(membership.status()).isEqualTo(MembershipStatus.INACTIVE);
        assertThat(membership.domainEvents()).hasSize(1);
        assertThat(membership.domainEvents().iterator().next())
                .isInstanceOf(TenantMembershipDeactivatedEvent.class);

        membership.clearDomainEvents();
        membership.activate();
        assertThat(membership.status()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(membership.domainEvents()).hasSize(1);
        assertThat(membership.domainEvents().iterator().next())
                .isInstanceOf(TenantMembershipActivatedEvent.class);
    }
}
