package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.MembershipCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.MembershipQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.UserQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AssignRolesToMembershipCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.DeactivateMembershipCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateMembershipCompensationCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Profile;
import com.andeva.atelier.platform.iam.domain.model.enums.AuthProvider;
import com.andeva.atelier.platform.iam.domain.model.enums.MembershipStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipsByTenantIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.AssignRolesResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.UpdateCompensationResource;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
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

import java.math.BigDecimal;
import java.time.Instant;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test suite for {@link MembershipsController}.
 * Verifies staff roster queries, RBAC role assignments, salary adjustments, and deactivations.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MembershipsController Unit Tests")
class MembershipsControllerTest {

    @Mock
    private MembershipQueryService membershipQueryService;

    @Mock
    private MembershipCommandService membershipCommandService;

    @Mock
    private UserQueryService userQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MembershipsController controller = new MembershipsController(
                membershipQueryService,
                membershipCommandService,
                userQueryService
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
    @DisplayName("GET /api/v1/tenants/{tenantId}/memberships returns staff roster with 200 OK")
    void getMembershipsSuccessfully() throws Exception {
        UUID userUuid = UUID.randomUUID();

        Role sampleRole = new Role(
                RoleId.generate(),
                TenantId.of(tenantUuid),
                "MECHANIC",
                "Mechanic",
                "Automotive mechanic",
                false,
                Collections.emptySet()
        );

        TenantMembership membership = TenantMembership.create(
                TenantId.of(tenantUuid),
                UserId.of(userUuid),
                SalaryType.FIXED,
                Money.of(2500, Currency.PEN),
                Set.of(sampleRole)
        );

        User user = User.registerWithLocalCredentials(
                EmailAddress.of("technician@precision.pe"),
                Password.of("$2a$12$e80yqZ67G60m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8"),
                PersonName.of("Jorge", "Benavides"),
                PhoneNumber.of("+51999888777")
        );

        when(membershipQueryService.handle(new GetMembershipsByTenantIdQuery(TenantId.of(tenantUuid))))
                .thenReturn(List.of(membership));
        when(userQueryService.handle(new GetUserByIdQuery(UserId.of(userUuid))))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(membership.id().value().toString()))
                .andExpect(jsonPath("$[0].email").value("technician@precision.pe"))
                .andExpect(jsonPath("$[0].employeeName").value("Jorge Benavides"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("PUT /api/v1/tenants/{tenantId}/memberships/{id}/roles assigns roles and returns 200 OK")
    void assignRolesSuccessfully() throws Exception {
        UUID membershipUuid = UUID.randomUUID();
        UUID roleUuid = UUID.randomUUID();
        AssignRolesResource resource = new AssignRolesResource(List.of(roleUuid));

        Role role = new Role(
                RoleId.of(roleUuid),
                TenantId.of(tenantUuid),
                "SENIOR_ADVISOR",
                "Senior Advisor",
                "Advisory role",
                false,
                Collections.emptySet()
        );

        TenantMembership membership = TenantMembership.create(
                TenantId.of(tenantUuid),
                UserId.of(UUID.randomUUID()),
                SalaryType.HOURLY,
                Money.of(0, Currency.PEN),
                Set.of(role)
        );

        when(membershipQueryService.handle(new GetMembershipByIdQuery(TenantMembershipId.of(membershipUuid))))
                .thenReturn(Optional.of(membership));
        when(membershipCommandService.handle(any(AssignRolesToMembershipCommand.class)))
                .thenReturn(Result.success(membership));

        mockMvc.perform(put("/api/v1/memberships/" + membershipUuid + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0].name").value("Senior Advisor"));

        verify(membershipCommandService).handle(any(AssignRolesToMembershipCommand.class));
    }

    @Test
    @DisplayName("PUT /api/v1/tenants/{tenantId}/memberships/{id}/compensation updates salary and returns 200 OK")
    void updateCompensationSuccessfully() throws Exception {
        UUID membershipUuid = UUID.randomUUID();
        UpdateCompensationResource resource = new UpdateCompensationResource(
                "FIXED",
                BigDecimal.valueOf(3200.00),
                "PEN"
        );

        Role sampleRole = new Role(
                RoleId.generate(),
                TenantId.of(tenantUuid),
                "MECHANIC",
                "Mechanic",
                "Automotive mechanic",
                false,
                Collections.emptySet()
        );

        TenantMembership membership = TenantMembership.create(
                TenantId.of(tenantUuid),
                UserId.of(UUID.randomUUID()),
                SalaryType.FIXED,
                Money.of(3200.00, Currency.PEN),
                Set.of(sampleRole)
        );

        when(membershipQueryService.handle(new GetMembershipByIdQuery(TenantMembershipId.of(membershipUuid))))
                .thenReturn(Optional.of(membership));
        when(membershipCommandService.handle(any(UpdateMembershipCompensationCommand.class)))
                .thenReturn(Result.success(membership));

        mockMvc.perform(put("/api/v1/memberships/" + membershipUuid + "/compensation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salaryType").value("FIXED"))
                .andExpect(jsonPath("$.baseSalary").value(3200.00))
                .andExpect(jsonPath("$.currency").value("PEN"));

        verify(membershipCommandService).handle(any(UpdateMembershipCompensationCommand.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/tenants/{tenantId}/memberships/{id} deactivates membership and returns 204 No Content")
    void deactivateMembershipSuccessfully() throws Exception {
        UUID membershipUuid = UUID.randomUUID();

        Role sampleRole = new Role(
                RoleId.generate(),
                TenantId.of(tenantUuid),
                "MECHANIC",
                "Mechanic",
                "Automotive mechanic",
                false,
                Collections.emptySet()
        );

        TenantMembership membership = TenantMembership.create(
                TenantId.of(tenantUuid),
                UserId.of(UUID.randomUUID()),
                SalaryType.FIXED,
                Money.of(2000, Currency.PEN),
                Set.of(sampleRole)
        );

        when(membershipQueryService.handle(new GetMembershipByIdQuery(TenantMembershipId.of(membershipUuid))))
                .thenReturn(Optional.of(membership));
        when(membershipCommandService.handle(any(DeactivateMembershipCommand.class)))
                .thenReturn(Result.success(null));

        mockMvc.perform(delete("/api/v1/memberships/" + membershipUuid))
                .andExpect(status().isNoContent());

        verify(membershipCommandService).handle(any(DeactivateMembershipCommand.class));
    }

    }
