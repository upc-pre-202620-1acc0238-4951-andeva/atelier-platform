package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.InvitationCommandService;
import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.application.internal.dto.InvitationValidationResult;
import com.andeva.atelier.platform.iam.application.queryservices.InvitationQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.RoleQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AcceptInvitationCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.InviteStaffCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Profile;
import com.andeva.atelier.platform.iam.domain.model.enums.AuthProvider;
import com.andeva.atelier.platform.iam.domain.model.enums.InvitationStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRoleByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.ValidateInvitationTokenQuery;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.AcceptInvitationResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.InviteStaffResource;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
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

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test suite for {@link InvitationsController}.
 * Verifies staff onboarding invitations, cryptographic token validation, and redemption.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InvitationsController Unit Tests")
class InvitationsControllerTest {

    @Mock
    private InvitationCommandService invitationCommandService;

    @Mock
    private InvitationQueryService invitationQueryService;

    @Mock
    private TenantQueryService tenantQueryService;

    @Mock
    private RoleQueryService roleQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        InvitationsController controller = new InvitationsController(
                invitationCommandService,
                invitationQueryService,
                tenantQueryService,
                roleQueryService
        );
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
    @DisplayName("POST /api/v1/invitations/tenant/{tenantId} returns 403 Forbidden on cross-tenant invitation")
    void inviteStaffCrossTenantForbidden() throws Exception {
        UUID foreignTenantUuid = UUID.randomUUID();
        InviteStaffResource resource = new InviteStaffResource("mechanic@precision.pe", UUID.randomUUID());

        mockMvc.perform(post("/api/v1/invitations/tenant/" + foreignTenantUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("POST /api/v1/invitations/tenant/{tenantId} dispatches invitation and returns 201 Created")
    void inviteStaffSuccessfully() throws Exception {
        UUID roleUuid = UUID.randomUUID();
        InviteStaffResource resource = new InviteStaffResource("mechanic@precision.pe", roleUuid);

        Invitation invitation = new Invitation(
                InvitationId.of(UUID.randomUUID()),
                TenantId.of(tenantUuid),
                EmailAddress.of("mechanic@precision.pe"),
                "secure-token-123",
                InvitationStatus.PENDING,
                RoleId.of(roleUuid),
                Instant.now().plus(Duration.ofDays(7))
        );

        when(invitationCommandService.handle(any(InviteStaffCommand.class))).thenReturn(Result.success(invitation));

        mockMvc.perform(post("/api/v1/invitations/tenant/" + tenantUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("mechanic@precision.pe"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(invitationCommandService).handle(any(InviteStaffCommand.class));
    }

    @Test
    @DisplayName("GET /api/v1/invitations/validate confirms authentic token with 200 OK")
    void validateTokenSuccessfully() throws Exception {
        String token = "secure-token-123";
        UUID tenantUuid = UUID.randomUUID();
        UUID roleUuid = UUID.randomUUID();

        Invitation invitation = new Invitation(
                InvitationId.of(UUID.randomUUID()),
                TenantId.of(tenantUuid),
                EmailAddress.of("collaborator@precision.pe"),
                token,
                InvitationStatus.PENDING,
                RoleId.of(roleUuid),
                Instant.now().plus(Duration.ofDays(5))
        );

        Tenant tenant = new Tenant(
                TenantId.of(tenantUuid),
                "Precision Motors",
                "PRECISION MOTORS SAC",
                TaxId.of("20456789014"),
                TenantStatus.ACTIVE,
                null,
                Collections.emptyList()
        );

        Role role = new Role(
                RoleId.of(roleUuid),
                TenantId.of(tenantUuid),
                "SENIOR_MECHANIC",
                "Senior Mechanic",
                "Master technician",
                false,
                Collections.emptySet()
        );

        when(invitationQueryService.handle(new ValidateInvitationTokenQuery(token)))
                .thenReturn(Optional.of(InvitationValidationResult.valid(invitation, "Precision Motors")));
        when(roleQueryService.handle(new GetRoleByIdQuery(RoleId.of(roleUuid)))).thenReturn(Optional.of(role));

        mockMvc.perform(get("/api/v1/invitations/validate").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.email").value("collaborator@precision.pe"))
                .andExpect(jsonPath("$.tenantName").value("Precision Motors"))
                .andExpect(jsonPath("$.roleName").value("Senior Mechanic"));
    }

    @Test
    @DisplayName("POST /api/v1/invitations/accept provisions collaborator credentials and returns 201 Created")
    void acceptInvitationSuccessfully() throws Exception {
        AcceptInvitationResource resource = new AcceptInvitationResource(
                "token-abc",
                "SecretPass2026*",
                "Mario",
                "Baracus",
                "+51987654321"
        );

        TenantId tenantId = TenantId.of(UUID.randomUUID());
        User user = User.registerWithLocalCredentials(
                EmailAddress.of("mario@precision.pe"),
                Password.of("$2a$12$e80yqZ67G60m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8"),
                PersonName.of("Mario", "Baracus"),
                PhoneNumber.of("+51987654321")
        );

        Tenant tenant = new Tenant(
                tenantId,
                "Precision Motors",
                "PRECISION MOTORS SAC",
                TaxId.of("20456789014"),
                TenantStatus.ACTIVE,
                null,
                Collections.emptyList()
        );

        AuthenticatedUser authUser = new AuthenticatedUser(
                user,
                "token.session.jwt",
                tenantId,
                Set.of("workshop:workorders:execute")
        );

        when(invitationCommandService.handle(any(AcceptInvitationCommand.class))).thenReturn(Result.success(authUser));
        when(tenantQueryService.handle(new GetTenantByIdQuery(tenantId))).thenReturn(Optional.of(tenant));

        mockMvc.perform(post("/api/v1/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user.id().value().toString()))
                .andExpect(jsonPath("$.token").value("token.session.jwt"))
                .andExpect(jsonPath("$.fullName").value("Mario Baracus"));
    }
}
