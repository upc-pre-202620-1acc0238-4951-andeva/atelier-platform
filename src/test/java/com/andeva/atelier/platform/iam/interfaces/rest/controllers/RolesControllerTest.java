package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.RoleCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.RoleQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateCustomRoleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.DeleteCustomRoleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetRoleToDefaultsCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateRolePermissionsCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.queries.GetAllPermissionsQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRolesByTenantIdQuery;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.CreateRoleResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.UpdateRolePermissionsResource;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRoleByIdQuery;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test suite for {@link RolesController}.
 * Verifies RBAC role management, custom role creation, permission reconfiguration, and platform privileges.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RolesController Unit Tests")
class RolesControllerTest {

    @Mock
    private RoleQueryService roleQueryService;

    @Mock
    private RoleCommandService roleCommandService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RolesController controller = new RolesController(roleQueryService, roleCommandService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(CustomUserDetails.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return new CustomUserDetails(
                                userUuid,
                                "admin@precision.pe",
                                "hashedPassword",
                                tenantUuid,
                                Collections.emptyList(),
                                true
                        );
                    }
                })
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/v1/tenants/{tenantId}/roles returns 403 Forbidden on cross-tenant access")
    void getRolesCrossTenantForbidden() throws Exception {
        UUID foreignTenantUuid = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/tenants/" + foreignTenantUuid + "/roles"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("GET /api/v1/tenants/{tenantId}/roles returns role list with 200 OK")
    void getRolesSuccessfully() throws Exception {
        UUID roleUuid = UUID.randomUUID();

        Role role = new Role(
                RoleId.of(roleUuid),
                TenantId.of(tenantUuid),
                "SENIOR_MECHANIC",
                "Senior Mechanic",
                "Master technician",
                false,
                Collections.emptySet()
        );

        when(roleQueryService.handle(new GetRolesByTenantIdQuery(TenantId.of(tenantUuid))))
                .thenReturn(List.of(role));

        mockMvc.perform(get("/api/v1/tenants/" + tenantUuid + "/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(roleUuid.toString()))
                .andExpect(jsonPath("$[0].name").value("Senior Mechanic"));
    }

    @Test
    @DisplayName("POST /api/v1/tenants/{tenantId}/roles creates custom role and returns 201 Created")
    void createRoleSuccessfully() throws Exception {
        UUID roleUuid = UUID.randomUUID();
        UUID permUuid = UUID.randomUUID();
        CreateRoleResource resource = new CreateRoleResource(
                "Quality Inspector",
                "Performs final vehicle inspection",
                List.of(permUuid)
        );

        Role role = new Role(
                RoleId.of(roleUuid),
                TenantId.of(tenantUuid),
                "QUALITY_INSPECTOR",
                "Quality Inspector",
                "Performs final vehicle inspection",
                false,
                Collections.emptySet()
        );

        when(roleCommandService.handle(any(CreateCustomRoleCommand.class))).thenReturn(Result.success(role));

        mockMvc.perform(post("/api/v1/tenants/" + tenantUuid + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(roleUuid.toString()))
                .andExpect(jsonPath("$.name").value("Quality Inspector"));

        verify(roleCommandService).handle(any(CreateCustomRoleCommand.class));
    }

    @Test
    @DisplayName("PUT /api/v1/tenants/{tenantId}/roles/{roleId}/permissions updates permissions and returns 200 OK")
    void updateRolePermissionsSuccessfully() throws Exception {
        UUID roleUuid = UUID.randomUUID();
        UUID permUuid = UUID.randomUUID();
        UpdateRolePermissionsResource resource = new UpdateRolePermissionsResource(List.of(permUuid));

        Role role = new Role(
                RoleId.of(roleUuid),
                TenantId.of(tenantUuid),
                "QUALITY_INSPECTOR",
                "Quality Inspector",
                "Performs final vehicle inspection",
                false,
                Set.of(new Permission(PermissionId.of(permUuid), "iam:roles:read", "Read roles", "IAM"))
        );

        when(roleQueryService.handle(new GetRoleByIdQuery(RoleId.of(roleUuid)))).thenReturn(Optional.of(role));
        when(roleCommandService.handle(any(UpdateRolePermissionsCommand.class))).thenReturn(Result.success(role));

        mockMvc.perform(put("/api/v1/tenants/" + tenantUuid + "/roles/" + roleUuid + "/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleUuid.toString()))
                .andExpect(jsonPath("$.permissions[0]").value("iam:roles:read"));

        verify(roleCommandService).handle(any(UpdateRolePermissionsCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/tenants/{tenantId}/roles/{roleId}/reset-defaults returns 200 OK")
    void resetRoleDefaultsSuccessfully() throws Exception {
        UUID roleUuid = UUID.randomUUID();

        Role role = new Role(
                RoleId.of(roleUuid),
                TenantId.of(tenantUuid),
                "OWNER",
                "OWNER",
                "Workshop owner",
                true,
                Collections.emptySet()
        );

        when(roleQueryService.handle(new GetRoleByIdQuery(RoleId.of(roleUuid)))).thenReturn(Optional.of(role));
        when(roleCommandService.handle(any(ResetRoleToDefaultsCommand.class))).thenReturn(Result.success(role));

        mockMvc.perform(post("/api/v1/tenants/" + tenantUuid + "/roles/" + roleUuid + "/reset-defaults"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("OWNER"))
                .andExpect(jsonPath("$.isSystemRole").value(true));

        verify(roleCommandService).handle(any(ResetRoleToDefaultsCommand.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/tenants/{tenantId}/roles/{roleId} deletes custom role and returns 204 No Content")
    void deleteRoleSuccessfully() throws Exception {
        UUID roleUuid = UUID.randomUUID();

        Role role = new Role(
                RoleId.of(roleUuid),
                TenantId.of(tenantUuid),
                "ASSISTANT",
                "Assistant",
                "Shop assistant",
                false,
                Collections.emptySet()
        );

        when(roleQueryService.handle(new GetRoleByIdQuery(RoleId.of(roleUuid)))).thenReturn(Optional.of(role));
        when(roleCommandService.handle(any(DeleteCustomRoleCommand.class))).thenReturn(Result.success(null));

        mockMvc.perform(delete("/api/v1/tenants/" + tenantUuid + "/roles/" + roleUuid))
                .andExpect(status().isNoContent());

        verify(roleCommandService).handle(any(DeleteCustomRoleCommand.class));
    }

    @Test
    @DisplayName("GET /api/v1/permissions returns all platform privileges with 200 OK")
    void getAllPermissionsSuccessfully() throws Exception {
        Permission permission = new Permission(
                PermissionId.of(UUID.randomUUID()),
                "iam:users:read",
                "View user accounts",
                "IAM"
        );

        when(roleQueryService.handle(any(GetAllPermissionsQuery.class))).thenReturn(List.of(permission));

        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("iam:users:read"))
                .andExpect(jsonPath("$[0].category").value("IAM"));
    }
}
