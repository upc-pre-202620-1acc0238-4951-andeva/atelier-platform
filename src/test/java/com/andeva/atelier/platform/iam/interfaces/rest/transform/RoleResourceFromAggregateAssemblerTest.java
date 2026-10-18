package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.RolePermissionResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.RoleResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for {@link RoleResourceFromAggregateAssembler}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("RoleResourceFromAggregateAssembler Unit Tests")
class RoleResourceFromAggregateAssemblerTest {

    @Test
    @DisplayName("Projects role identity, tenant and permissions as id/name/category objects sorted by name")
    void toResourceFromAggregateProjectsPermissionObjects() {
        UUID roleUuid = UUID.randomUUID();
        UUID tenantUuid = UUID.randomUUID();
        UUID trackTimeUuid = UUID.randomUUID();
        UUID rolesReadUuid = UUID.randomUUID();
        Role role = new Role(
                RoleId.of(roleUuid),
                TenantId.of(tenantUuid),
                "ROLE_MECHANIC",
                "Técnico Mecánico",
                "Ejecución directa de labores mecánicas y diagnóstico en bahía",
                true,
                Set.of(
                        new Permission(PermissionId.of(trackTimeUuid), "operations:tasks:track_time", "Registrar tiempos", "OPERATIONS"),
                        new Permission(PermissionId.of(rolesReadUuid), "iam:roles:read", "Consultar roles", "IAM")
                )
        );

        RoleResource resource = RoleResourceFromAggregateAssembler.toResourceFromAggregate(role);

        assertThat(resource.id()).isEqualTo(roleUuid);
        assertThat(resource.tenantId()).isEqualTo(tenantUuid);
        assertThat(resource.code()).isEqualTo("ROLE_MECHANIC");
        assertThat(resource.isSystemRole()).isTrue();
        assertThat(resource.permissions()).containsExactly(
                new RolePermissionResource(rolesReadUuid, "iam:roles:read", "iam"),
                new RolePermissionResource(trackTimeUuid, "operations:tasks:track_time", "operations")
        );
    }

    @Test
    @DisplayName("Derives the category as the lower case prefix before the first ':'")
    void categoryIsLowerCasePrefixOfName() {
        assertThat(PermissionResourceFromEntityAssembler.categoryOf("operations:tasks:track_time")).isEqualTo("operations");
        assertThat(PermissionResourceFromEntityAssembler.categoryOf("IAM:roles:read")).isEqualTo("iam");
    }
}
