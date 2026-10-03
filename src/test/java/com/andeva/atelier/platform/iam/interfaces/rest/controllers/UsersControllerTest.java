package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.UserCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.UserQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateUserProfileCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Profile;
import com.andeva.atelier.platform.iam.domain.model.enums.AuthProvider;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.UserStatus;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.UpdateProfileResource;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test suite for {@link UsersController}.
 * Verifies personal session profile resolution and demographic contact details updates.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsersController Unit Tests")
class UsersControllerTest {

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private TenantQueryService tenantQueryService;

    @Mock
    private UserCommandService userCommandService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final UUID userUuid = UUID.randomUUID();
    private final UUID tenantUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        UsersController controller = new UsersController(userQueryService, tenantQueryService, userCommandService);
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
                                List.of(new SimpleGrantedAuthority("iam:users:read")),
                                true
                        );
                    }
                })
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/v1/users/me returns authenticated caller profile and permissions with 200 OK")
    void getCurrentUserSuccessfully() throws Exception {
        Profile profile = new Profile(UserId.of(userUuid), PersonName.of("Carlos", "Mendoza"), PhoneNumber.of("+51987654321"));
        User user = new User(
                UserId.of(userUuid),
                EmailAddress.of("admin@precision.pe"),
                Password.of("$2a$12$e80yqZ67G60m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8"),
                AuthProvider.LOCAL,
                null,
                null,
                UserStatus.ACTIVE,
                profile,
                Collections.emptyList()
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

        when(userQueryService.handle(new GetUserByIdQuery(UserId.of(userUuid))))
                .thenReturn(Optional.of(user));
        when(tenantQueryService.handle(new GetTenantByIdQuery(new TenantId(tenantUuid))))
                .thenReturn(Optional.of(tenant));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userUuid.toString()))
                .andExpect(jsonPath("$.email").value("admin@precision.pe"))
                .andExpect(jsonPath("$.fullName").value("Carlos Mendoza"))
                .andExpect(jsonPath("$.activeTenant.name").value("Precision Motors"))
                .andExpect(jsonPath("$.permissions[0]").value("iam:users:read"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me/profile updates demographic information with 200 OK")
    void updateProfileSuccessfully() throws Exception {
        UpdateProfileResource resource = new UpdateProfileResource("Carlos Alberto", "Mendoza Flores", "+51912345678");

        Profile updatedProfile = new Profile(UserId.of(userUuid), PersonName.of("Carlos Alberto", "Mendoza Flores"), PhoneNumber.of("+51912345678"));
        User updatedUser = new User(
                UserId.of(userUuid),
                EmailAddress.of("admin@precision.pe"),
                Password.of("$2a$12$e80yqZ67G60m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8m8m8e8"),
                AuthProvider.LOCAL,
                null,
                null,
                UserStatus.ACTIVE,
                updatedProfile,
                Collections.emptyList()
        );

        when(userCommandService.handle(any(UpdateUserProfileCommand.class)))
                .thenReturn(Result.success(updatedUser));

        mockMvc.perform(put("/api/v1/users/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Carlos Alberto"))
                .andExpect(jsonPath("$.lastName").value("Mendoza Flores"))
                .andExpect(jsonPath("$.phone").value("+51912345678"));

        verify(userCommandService).handle(any(UpdateUserProfileCommand.class));
    }
}
