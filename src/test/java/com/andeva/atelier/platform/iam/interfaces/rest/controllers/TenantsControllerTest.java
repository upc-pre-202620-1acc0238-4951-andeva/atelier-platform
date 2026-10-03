package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.TenantCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateTenantProfileCommand;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.UpdateTenantProfileResource;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
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

import java.time.Instant;
import java.util.Collections;
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
 * Unit test suite for {@link TenantsController}.
 * Verifies profile retrieval and updates for multi-tenant automotive workshops.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantsController Unit Tests")
class TenantsControllerTest {

    @Mock
    private TenantQueryService tenantQueryService;

    @Mock
    private TenantCommandService tenantCommandService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantsController controller = new TenantsController(tenantQueryService, tenantCommandService);
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
    @DisplayName("GET /api/v1/tenants/current returns active tenant details with 200 OK")
    void getCurrentTenantSuccessfully() throws Exception {
        Tenant tenant = new Tenant(
                TenantId.of(tenantUuid),
                "Precision Motors",
                "PRECISION MOTORS SAC",
                TaxId.of("20456789014"),
                TenantStatus.ACTIVE,
                null,
                Collections.emptyList()
        );

        when(tenantQueryService.handle(new GetTenantByIdQuery(TenantId.of(tenantUuid))))
                .thenReturn(Optional.of(tenant));

        mockMvc.perform(get("/api/v1/tenants/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenantUuid.toString()))
                .andExpect(jsonPath("$.name").value("Precision Motors"))
                .andExpect(jsonPath("$.legalName").value("PRECISION MOTORS SAC"))
                .andExpect(jsonPath("$.taxId").value("20456789014"));
    }

    @Test
    @DisplayName("PUT /api/v1/tenants/current updates tenant profile with 200 OK")
    void updateCurrentTenantProfileSuccessfully() throws Exception {
        UpdateTenantProfileResource resource = new UpdateTenantProfileResource(
                "Precision Motors Global",
                "PRECISION MOTORS GLOBAL S.A.C."
        );

        Tenant updatedTenant = new Tenant(
                TenantId.of(tenantUuid),
                "Precision Motors Global",
                "PRECISION MOTORS GLOBAL S.A.C.",
                TaxId.of("20456789014"),
                TenantStatus.ACTIVE,
                null,
                Collections.emptyList()
        );

        when(tenantCommandService.handle(any(UpdateTenantProfileCommand.class)))
                .thenReturn(Result.success(updatedTenant));

        mockMvc.perform(put("/api/v1/tenants/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Precision Motors Global"))
                .andExpect(jsonPath("$.legalName").value("PRECISION MOTORS GLOBAL S.A.C."));

        verify(tenantCommandService).handle(any(UpdateTenantProfileCommand.class));
    }

    @Test
    @DisplayName("GET /api/v1/tenants/current returns 404 Not Found when tenant is missing")
    void getCurrentTenantReturnsNotFound() throws Exception {
        when(tenantQueryService.handle(new GetTenantByIdQuery(TenantId.of(tenantUuid))))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/tenants/current"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
