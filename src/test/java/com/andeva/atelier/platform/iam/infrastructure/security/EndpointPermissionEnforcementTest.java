package com.andeva.atelier.platform.iam.infrastructure.security;

import com.andeva.atelier.platform.billing.interfaces.rest.controllers.TenantSubscriptionsController;
import com.andeva.atelier.platform.crm.interfaces.rest.controllers.CustomersController;
import com.andeva.atelier.platform.hr.interfaces.rest.controllers.AttendanceController;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PermissionCatalog;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iam.interfaces.rest.controllers.MembershipsController;
import com.andeva.atelier.platform.inventory.interfaces.rest.controllers.PurchaseOrdersController;
import com.andeva.atelier.platform.invoicing.interfaces.rest.controllers.ElectronicVouchersController;
import com.andeva.atelier.platform.iot.interfaces.rest.controllers.Obd2DevicesController;
import com.andeva.atelier.platform.operations.interfaces.rest.controllers.WorkOrdersController;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * Verifies with real method security that endpoints are authorized by the atomic permissions granted to the
 * caller's roles: one endpoint per bounded context, a factory role lacking the permission gets 403, a role
 * holding it passes, and revoking the permission from a role closes the endpoint.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Endpoint Permission Enforcement Tests")
class EndpointPermissionEnforcementTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID ANY_ID = UUID.randomUUID();
    private static final List<Class<?>> CONTROLLERS = List.of(
            MembershipsController.class, CustomersController.class, WorkOrdersController.class,
            PurchaseOrdersController.class, AttendanceController.class, ElectronicVouchersController.class,
            TenantSubscriptionsController.class, Obd2DevicesController.class);

    private static AnnotationConfigApplicationContext context;
    private static MockMvc mockMvc;
    private static List<Permission> catalog;

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityConfiguration {
    }

    @BeforeAll
    static void setUpSecuredControllers() {
        context = new AnnotationConfigApplicationContext();
        context.register(MethodSecurityConfiguration.class);
        for (Class<?> controller : CONTROLLERS) {
            registerWithMockedDependencies(controller);
        }
        context.refresh();

        Object[] proxies = CONTROLLERS.stream().map(context::getBean).toArray();
        mockMvc = MockMvcBuilders.standaloneSetup(proxies)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        catalog = PermissionCatalog.entries().stream()
                .map(entry -> Permission.create(entry.name(), entry.description(), entry.category()))
                .toList();
    }

    @AfterAll
    static void closeContext() {
        context.close();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    static Stream<Arguments> securedEndpoints() {
        return Stream.of(
                Arguments.of("iam", HttpMethod.GET, "/api/v1/memberships", "iam:members:read",
                        RoleTemplate.ROLE_WORKSHOP_ADMINISTRATOR, RoleTemplate.ROLE_MECHANIC),
                Arguments.of("crm", HttpMethod.GET, "/api/v1/customers", "crm:customers:read",
                        RoleTemplate.ROLE_RECEPTIONIST, RoleTemplate.ROLE_MECHANIC),
                Arguments.of("operations", HttpMethod.POST, "/api/v1/work-orders/" + ANY_ID + "/start",
                        "operations:work_orders:update", RoleTemplate.ROLE_SERVICE_ADVISOR, RoleTemplate.ROLE_MECHANIC),
                Arguments.of("inventory", HttpMethod.GET, "/api/v1/inventory/purchase-orders",
                        "inventory:purchase_orders:read", RoleTemplate.ROLE_INVENTORY_MANAGER, RoleTemplate.ROLE_CASHIER),
                Arguments.of("hr", HttpMethod.GET, "/api/v1/hr/attendances/branch/" + ANY_ID + "/daily",
                        "hr:attendance:audit_all", RoleTemplate.ROLE_CHIEF_MECHANIC, RoleTemplate.ROLE_MECHANIC),
                Arguments.of("invoicing", HttpMethod.GET, "/api/v1/invoicing/vouchers", "invoicing:invoices:read",
                        RoleTemplate.ROLE_CASHIER, RoleTemplate.ROLE_RECEPTIONIST),
                Arguments.of("billing", HttpMethod.GET, "/api/v1/billing/subscriptions/me",
                        "billing:subscriptions:read", RoleTemplate.ROLE_WORKSHOP_OWNER, RoleTemplate.ROLE_CHIEF_MECHANIC),
                Arguments.of("iot", HttpMethod.GET, "/api/v1/iot/devices", "iot:devices:read",
                        RoleTemplate.ROLE_MECHANIC, RoleTemplate.ROLE_RECEPTIONIST)
        );
    }

    @ParameterizedTest(name = "[{0}] {1} {2}")
    @MethodSource("securedEndpoints")
    @DisplayName("Role without the permission gets 403, role with it passes, revoking it closes the endpoint")
    void shouldEnforcePermissionPerEndpoint(String context, HttpMethod method, String path, String permission,
                                            RoleTemplate allowed, RoleTemplate denied) throws Exception {
        assertThat(allowed.defaultPermissionNames()).as("%s grants %s", allowed, permission).contains(permission);
        assertThat(denied.defaultPermissionNames()).as("%s lacks %s", denied, permission).doesNotContain(permission);

        Role deniedRole = factoryRole(denied);
        assertThat(perform(method, path, deniedRole)).as("%s on %s", denied, path).isEqualTo(403);

        Role allowedRole = factoryRole(allowed);
        assertThat(perform(method, path, allowedRole)).as("%s on %s", allowed, path).isNotEqualTo(403);

        allowedRole.revokePermission(permissionNamed(permission).id());
        assertThat(perform(method, path, allowedRole)).as("%s after revoking %s", allowed, permission).isEqualTo(403);
    }

    @Test
    @DisplayName("A mechanic cannot start a work order unless the workshop grants the permission to the role")
    void shouldHonourSovereignRoleEdits() throws Exception {
        String path = "/api/v1/work-orders/" + ANY_ID + "/start";
        Role mechanic = factoryRole(RoleTemplate.ROLE_MECHANIC);
        assertThat(perform(HttpMethod.POST, path, mechanic)).isEqualTo(403);

        mechanic.grantPermission(permissionNamed("operations:work_orders:update"));
        assertThat(perform(HttpMethod.POST, path, mechanic)).isNotEqualTo(403);
    }

    @Test
    @DisplayName("A custom role is authorized only by the permissions it holds")
    void shouldAuthorizeCustomRoleByPermissions() throws Exception {
        Role custom = Role.createCustom(TenantId.of(TENANT_ID), "Caja Rápida", "Custom cashier",
                Set.of(permissionNamed("invoicing:invoices:read")));

        assertThat(perform(HttpMethod.GET, "/api/v1/invoicing/vouchers", custom)).isNotEqualTo(403);
        assertThat(perform(HttpMethod.GET, "/api/v1/customers", custom)).isEqualTo(403);
    }

    private static int perform(HttpMethod method, String path, Role... roles) throws Exception {
        authenticateWith(roles);
        MockHttpServletRequestBuilder builder = request(method, path).header("X-Tenant-Id", TENANT_ID.toString());
        return mockMvc.perform(builder).andReturn().getResponse().getStatus();
    }

    /**
     * Authenticates with the same authorities the JWT carries: factory role codes plus granted permission names.
     */
    private static void authenticateWith(Role... roles) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : roles) {
            if (role.code() != null) {
                authorities.add(new SimpleGrantedAuthority(role.code()));
            }
            role.permissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.name())));
        }
        CustomUserDetails principal = new CustomUserDetails(
                UUID.randomUUID(), "staff@atelier.pe", "hash", TENANT_ID, authorities, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    private static Role factoryRole(RoleTemplate template) {
        return Role.fromTemplate(TenantId.of(TENANT_ID), template, catalog);
    }

    private static Permission permissionNamed(String name) {
        return catalog.stream().filter(p -> p.name().equals(name)).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerWithMockedDependencies(Class<T> type) {
        Constructor<?> constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        List<Object> arguments = new ArrayList<>();
        for (Class<?> parameter : constructor.getParameterTypes()) {
            arguments.add(Mockito.mock(parameter));
        }
        context.registerBean(type, () -> {
            try {
                return (T) constructor.newInstance(arguments.toArray());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
