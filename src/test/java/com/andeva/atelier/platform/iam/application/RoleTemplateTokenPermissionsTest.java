package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.GoogleIdentityGateway;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BearerTokenService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PermissionCatalog;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that a user holding a factory role receives in the JWT exactly the permissions of its template.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Role Template JWT Permissions Tests")
class RoleTemplateTokenPermissionsTest {

    private static final String RAW_PASSWORD = "SecretPassword123!";

    @Mock
    private UserRepository userRepository;
    @Mock
    private TenantMembershipRepository membershipRepository;
    @Mock
    private BCryptHashingService hashingService;
    @Mock
    private BearerTokenService bearerTokenService;
    @Mock
    private GoogleIdentityGateway googleIdentityGateway;

    private UserCommandServiceImpl userCommandService;

    private final EmailAddress email = EmailAddress.of("staff@atelier.pe");
    private final Password password = Password.of("$2a$12$abcdefghijklmnopqrstuvwxABCDEFGHIJKLMNOPQRSTUVWXYZ012");
    private final TenantId tenantId = TenantId.generate();

    @BeforeEach
    void setUp() {
        userCommandService = new UserCommandServiceImpl(
                userRepository, membershipRepository, hashingService, bearerTokenService, googleIdentityGateway);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(RoleTemplate.class)
    @DisplayName("Should issue a JWT with exactly the template permissions plus the role code")
    void shouldIssueExactlyTemplatePermissions(RoleTemplate template) {
        Role role = Role.fromTemplate(tenantId, template, seededCatalog());

        Set<String> tokenPermissions = authenticateWith(role);

        Set<String> expected = new HashSet<>(template.defaultPermissionNames());
        expected.add(template.code());
        assertThat(tokenPermissions).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should keep owner-only permissions out of the mechanic token")
    void shouldNotLeakPrivilegedPermissionsToMechanic() {
        Role mechanic = Role.fromTemplate(tenantId, RoleTemplate.ROLE_MECHANIC, seededCatalog());

        Set<String> tokenPermissions = authenticateWith(mechanic);

        assertThat(tokenPermissions)
                .contains("operations:tasks:track_time", "hr:attendance:clock_in", "ROLE_MECHANIC")
                .doesNotContain("billing:subscriptions:manage_stripe", "iam:members:invite",
                        "invoicing:payments:create", "operations:work_orders:create", "ROLE_WORKSHOP_OWNER");
    }

    @Test
    @DisplayName("Should issue a custom role token with its permissions only and no derived role authority")
    void shouldIssueCustomRolePermissionsOnly() {
        Permission partsRead = Permission.create("inventory:parts:read", "Read parts", "INVENTORY");
        Role custom = Role.createCustom(tenantId, "Workshop Admin", "Name clashing with a factory code", Set.of(partsRead));

        Set<String> tokenPermissions = authenticateWith(custom);

        assertThat(tokenPermissions).containsExactly("inventory:parts:read");
    }

    @Test
    @DisplayName("Should issue the union of permissions for multi-role memberships")
    void shouldMergeMultipleRoles() {
        List<Permission> catalog = seededCatalog();
        Role advisor = Role.fromTemplate(tenantId, RoleTemplate.ROLE_SERVICE_ADVISOR, catalog);
        Role inventory = Role.fromTemplate(tenantId, RoleTemplate.ROLE_INVENTORY_MANAGER, catalog);

        Set<String> tokenPermissions = authenticateWith(advisor, inventory);

        Set<String> expected = new HashSet<>(RoleTemplate.ROLE_SERVICE_ADVISOR.defaultPermissionNames());
        expected.addAll(RoleTemplate.ROLE_INVENTORY_MANAGER.defaultPermissionNames());
        expected.add("ROLE_SERVICE_ADVISOR");
        expected.add("ROLE_INVENTORY_MANAGER");
        assertThat(tokenPermissions).containsExactlyInAnyOrderElementsOf(expected);
    }

    /**
     * The persisted catalog as seeded on startup, plus a stray permission that no template grants.
     */
    private static List<Permission> seededCatalog() {
        List<Permission> catalog = new ArrayList<>();
        PermissionCatalog.entries().forEach(entry ->
                catalog.add(Permission.create(entry.name(), entry.description(), entry.category())));
        catalog.add(Permission.create("billing:plans:write", "Platform plan management", "BILLING"));
        return catalog;
    }

    @SuppressWarnings("unchecked")
    private Set<String> authenticateWith(Role... roles) {
        User user = User.registerWithLocalCredentials(
                email, password, PersonName.of("Pedro", "Quispe"), PhoneNumber.of("+51987654321"));
        user.activate();
        TenantMembership membership = TenantMembership.create(
                tenantId, user.id(), SalaryType.FIXED, Money.soles(1500.0), Set.of(roles));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(hashingService.matches(RAW_PASSWORD, password.hashedValue())).thenReturn(true);
        when(membershipRepository.findByUserId(user.id())).thenReturn(List.of(membership));
        when(bearerTokenService.generateToken(eq(user), eq(tenantId), any())).thenReturn("jwt");

        AuthenticatedUser authenticated = userCommandService
                .handle(new AuthenticateUserCommand(email, RAW_PASSWORD))
                .getOrThrow();

        ArgumentCaptor<Collection<String>> claims = ArgumentCaptor.forClass(Collection.class);
        verify(bearerTokenService).generateToken(eq(user), eq(tenantId), claims.capture());
        assertThat(authenticated.permissions()).containsExactlyInAnyOrderElementsOf(claims.getValue());
        return new HashSet<>(claims.getValue());
    }
}
