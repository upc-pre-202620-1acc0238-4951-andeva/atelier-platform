package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.BranchCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.BranchQueryService;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateBranchCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateBranchLocationCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchesByTenantIdQuery;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.CreateBranchResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.UpdateBranchLocationResource;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test suite for {@link BranchesController}.
 * Verifies listing, creation, and geodesic coordinate updates for workshop physical branches.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BranchesController Unit Tests")
class BranchesControllerTest {

    @Mock
    private BranchQueryService branchQueryService;

    @Mock
    private BranchCommandService branchCommandService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        BranchesController controller = new BranchesController(branchQueryService, branchCommandService);
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
    @DisplayName("GET /api/v1/branches returns branch collection with 200 OK")
    void getBranchesSuccessfully() throws Exception {
        Branch branch = new Branch(
                BranchId.of(UUID.randomUUID()),
                TenantId.of(tenantUuid),
                "Sede Surco",
                "0001",
                GeoPoint.of(-12.1458, -76.9912),
                100,
                true
        );

        when(branchQueryService.handle(new GetBranchesByTenantIdQuery(TenantId.of(tenantUuid))))
                .thenReturn(List.of(branch));

        mockMvc.perform(get("/api/v1/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sede Surco"))
                .andExpect(jsonPath("$[0].sunatCode").value("0001"))
                .andExpect(jsonPath("$[0].geofenceRadiusMeters").value(100));
    }

    @Test
    @DisplayName("POST /api/v1/branches creates physical branch and returns 201 Created")
    void createBranchSuccessfully() throws Exception {
        CreateBranchResource resource = new CreateBranchResource(
                "Sede Miraflores",
                "0002",
                -12.1215,
                -77.0298,
                80
        );

        Branch branch = new Branch(
                BranchId.of(UUID.randomUUID()),
                TenantId.of(tenantUuid),
                "Sede Miraflores",
                "0002",
                GeoPoint.of(-12.1215, -77.0298),
                80,
                true
        );

        when(branchCommandService.handle(any(CreateBranchCommand.class))).thenReturn(Result.success(branch));

        mockMvc.perform(post("/api/v1/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sede Miraflores"))
                .andExpect(jsonPath("$.latitude").value(-12.1215))
                .andExpect(jsonPath("$.longitude").value(-77.0298));

        verify(branchCommandService).handle(any(CreateBranchCommand.class));
    }

    @Test
    @DisplayName("GET /api/v1/branches/{id} returns branch by ID with 200 OK")
    void getBranchByIdSuccessfully() throws Exception {
        UUID branchUuid = UUID.randomUUID();
        Branch branch = new Branch(
                BranchId.of(branchUuid),
                TenantId.of(tenantUuid),
                "Sede Principal",
                "0000",
                GeoPoint.of(-12.0931, -77.0465),
                150,
                true
        );

        when(branchQueryService.handle(new GetBranchByIdQuery(BranchId.of(branchUuid))))
                .thenReturn(Optional.of(branch));

        mockMvc.perform(get("/api/v1/branches/" + branchUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(branchUuid.toString()))
                .andExpect(jsonPath("$.name").value("Sede Principal"));
    }

    @Test
    @DisplayName("PUT /api/v1/branches/{id}/location updates coordinates and returns 200 OK")
    void updateBranchLocationSuccessfully() throws Exception {
        UUID branchUuid = UUID.randomUUID();
        UpdateBranchLocationResource resource = new UpdateBranchLocationResource(-12.0950, -77.0480, 200);

        Branch existingBranch = new Branch(
                BranchId.of(branchUuid),
                TenantId.of(tenantUuid),
                "Sede Principal",
                "0000",
                GeoPoint.of(-12.0931, -77.0465),
                150,
                true
        );

        Branch updatedBranch = new Branch(
                BranchId.of(branchUuid),
                TenantId.of(tenantUuid),
                "Sede Principal",
                "0000",
                GeoPoint.of(-12.0950, -77.0480),
                200,
                true
        );

        when(branchQueryService.handle(new GetBranchByIdQuery(BranchId.of(branchUuid))))
                .thenReturn(Optional.of(existingBranch));
        when(branchCommandService.handle(any(UpdateBranchLocationCommand.class)))
                .thenReturn(Result.success(updatedBranch));

        mockMvc.perform(put("/api/v1/branches/" + branchUuid + "/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.geofenceRadiusMeters").value(200));

        verify(branchCommandService).handle(any(UpdateBranchLocationCommand.class));
    }
}
