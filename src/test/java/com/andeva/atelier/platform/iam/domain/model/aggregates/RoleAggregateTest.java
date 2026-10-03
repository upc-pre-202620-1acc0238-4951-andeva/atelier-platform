package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.events.RoleCreatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.RoleUpdatedEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for {@link Role} aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Role Aggregate Root Unit Tests")
class RoleAggregateTest {

    private final TenantId tenantId = TenantId.generate();
    private final Permission perm1 = Permission.of("inventory:items:read", "Read inventory items", "INVENTORY");
    private final Permission perm2 = Permission.of("inventory:items:create", "Create inventory items", "INVENTORY");

    @Test
    @DisplayName("Should create custom role with isSystemRole=false and register RoleCreatedEvent")
    void shouldCreateCustomRole() {
        Role role = Role.createCustom(tenantId, "Auxiliar de Almacén", "Apoyo en stock", Set.of(perm1));

        assertThat(role.id()).isNotNull();
        assertThat(role.tenantId()).isEqualTo(tenantId);
        assertThat(role.name()).isEqualTo("Auxiliar de Almacén");
        assertThat(role.code()).isNull();
        assertThat(role.isSystemRole()).isFalse();
        assertThat(role.permissions()).containsExactly(perm1);

        assertThat(role.domainEvents()).hasSize(1);
        assertThat(role.domainEvents().iterator().next()).isInstanceOf(RoleCreatedEvent.class);
    }

    @Test
    @DisplayName("Should provision system factory role from template with isSystemRole=true")
    void shouldProvisionFromTemplate() {
        Role role = Role.provisionFromTemplate(tenantId, RoleTemplate.ROLE_WORKSHOP_ADMIN, Set.of(perm1, perm2));

        assertThat(role.id()).isNotNull();
        assertThat(role.tenantId()).isEqualTo(tenantId);
        assertThat(role.name()).isEqualTo("Workshop Administrator");
        assertThat(role.code()).isEqualTo("ROLE_WORKSHOP_ADMIN");
        assertThat(role.isSystemRole()).isTrue();
        assertThat(role.permissions()).containsExactlyInAnyOrder(perm1, perm2);

        assertThat(role.domainEvents()).hasSize(1);
        assertThat(role.domainEvents().iterator().next()).isInstanceOf(RoleCreatedEvent.class);
    }

    @Test
    @DisplayName("Should update permissions and register RoleUpdatedEvent")
    void shouldUpdatePermissions() {
        Role role = Role.createCustom(tenantId, "Técnico Pintor", "Pintura y acabado", Set.of(perm1));
        role.clearDomainEvents();

        role.updatePermissions(Set.of(perm2));
        assertThat(role.permissions()).containsExactly(perm2);

        assertThat(role.domainEvents()).hasSize(1);
        assertThat(role.domainEvents().iterator().next()).isInstanceOf(RoleUpdatedEvent.class);
    }

    @Test
    @DisplayName("Should reset system role to template defaults and register RoleUpdatedEvent")
    void shouldResetSystemRoleToTemplate() {
        Role role = Role.provisionFromTemplate(tenantId, RoleTemplate.ROLE_MECHANIC, Set.of(perm1));
        role.grantPermission(perm2);
        role.clearDomainEvents();

        role.resetToTemplate(RoleTemplate.ROLE_MECHANIC, Set.of(perm1));
        assertThat(role.name()).isEqualTo("Mechanic");
        assertThat(role.permissions()).containsExactly(perm1);

        assertThat(role.domainEvents()).hasSize(1);
        assertThat(role.domainEvents().iterator().next()).isInstanceOf(RoleUpdatedEvent.class);
    }

    @Test
    @DisplayName("Should reject resetting custom roles to template")
    void shouldRejectResettingCustomRole() {
        Role customRole = Role.createCustom(tenantId, "Asistente", "General", Set.of(perm1));

        assertThatThrownBy(() -> customRole.resetToTemplate(RoleTemplate.ROLE_MECHANIC, Set.of(perm1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only protected system roles can be reset");
    }

    @Test
    @DisplayName("Should grant and revoke individual permissions")
    void shouldGrantAndRevokeIndividualPermission() {
        Role role = Role.createCustom(tenantId, "Técnico Eléctrico", "Diagnóstico escáner", Set.of(perm1));

        role.grantPermission(perm2);
        assertThat(role.permissions()).containsExactlyInAnyOrder(perm1, perm2);

        role.revokePermission(perm1.id());
        assertThat(role.permissions()).containsExactly(perm2);
    }
}
