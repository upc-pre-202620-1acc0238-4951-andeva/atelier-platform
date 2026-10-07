package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.VehicleFaultCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.VehicleFaultQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.VehicleFaultNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.commands.RegisterVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.ResolveVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.iot.interfaces.rest.advice.IoTExceptionHandler;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.RegisterVehicleFaultRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.VehicleFaultResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link VehicleFaultsController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleFaultsController Unit Tests")
class VehicleFaultsControllerTest {

    @Mock
    private VehicleFaultCommandService faultCommandService;

    @Mock
    private VehicleFaultQueryService faultQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();
    private final UUID vehicleUuid = UUID.randomUUID();
    private final UUID faultUuid = UUID.randomUUID();
    private FaultId faultId;
    private VehicleFault fault;

    @BeforeEach
    void setUp() {
        VehicleFaultResourceAssembler assembler = new VehicleFaultResourceAssembler();
        VehicleFaultsController controller = new VehicleFaultsController(
                faultCommandService,
                faultQueryService,
                assembler
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(CustomUserDetails.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter,
                                                  ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest,
                                                  WebDataBinderFactory binderFactory) {
                        return new CustomUserDetails(
                                userUuid,
                                "mechanic@andeva.pe",
                                "hashedPassword",
                                tenantUuid,
                                Collections.emptyList(),
                                true
                        );
                    }
                })
                .setControllerAdvice(new IoTExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        faultId = FaultId.of(faultUuid);
        fault = VehicleFault.detect(
                VehicleId.of(vehicleUuid),
                TenantId.of(tenantUuid),
                DtcCode.of("P0300"),
                FaultSeverity.CRITICAL,
                "Random/Multiple Cylinder Misfire Detected"
        );
    }

    @Test
    @DisplayName("Should register vehicle fault successfully with 201 Created")
    void shouldRegisterVehicleFaultSuccessfully() throws Exception {
        RegisterVehicleFaultRequest request = new RegisterVehicleFaultRequest(
                vehicleUuid,
                "P0300",
                "CRITICAL",
                "Random/Multiple Cylinder Misfire Detected"
        );

        when(faultCommandService.handle(any(RegisterVehicleFaultCommand.class))).thenReturn(faultId);
        when(faultQueryService.findById(faultId)).thenReturn(Optional.of(fault));

        mockMvc.perform(post("/api/v1/iot/faults")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/iot/faults/" + faultId.value()))
                .andExpect(jsonPath("$.dtcCode").value("P0300"))
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.isResolved").value(false));

        verify(faultCommandService).handle(any(RegisterVehicleFaultCommand.class));
    }

    @Test
    @DisplayName("Should return 400 when registering fault with invalid DTC code")
    void shouldReturnBadRequestWhenDtcCodeIsInvalid() throws Exception {
        RegisterVehicleFaultRequest request = new RegisterVehicleFaultRequest(
                vehicleUuid,
                "INVALID-DTC",
                "CRITICAL",
                "Invalid code format"
        );

        mockMvc.perform(post("/api/v1/iot/faults")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    @DisplayName("Should return active faults for a vehicle")
    void shouldReturnActiveFaultsForVehicle() throws Exception {
        VehicleId vehicleId = VehicleId.of(vehicleUuid);
        when(faultQueryService.getActiveFaultsByVehicle(vehicleId)).thenReturn(List.of(fault));

        mockMvc.perform(get("/api/v1/iot/faults/vehicle/{vehicleId}/active", vehicleUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehicleId").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$[0].dtcCode").value("P0300"))
                .andExpect(jsonPath("$[0].severity").value("CRITICAL"))
                .andExpect(jsonPath("$[0].isResolved").value(false));
    }

    @Test
    @DisplayName("Should return 403 when active faults belong to different tenant")
    void shouldReturnForbiddenWhenActiveFaultsBelongToDifferentTenant() throws Exception {
        UUID otherTenant = UUID.randomUUID();
        VehicleFault crossTenantFault = VehicleFault.detect(
                VehicleId.of(vehicleUuid),
                TenantId.of(otherTenant),
                DtcCode.of("P0300"),
                FaultSeverity.CRITICAL,
                "Cross-tenant test"
        );

        VehicleId vehicleId = VehicleId.of(vehicleUuid);
        when(faultQueryService.getActiveFaultsByVehicle(vehicleId)).thenReturn(List.of(crossTenantFault));

        mockMvc.perform(get("/api/v1/iot/faults/vehicle/{vehicleId}/active", vehicleUuid))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access Denied"));
    }

    @Test
    @DisplayName("Should resolve vehicle fault successfully")
    void shouldResolveVehicleFaultSuccessfully() throws Exception {
        VehicleFault resolvedFault = VehicleFault.detect(
                VehicleId.of(vehicleUuid),
                TenantId.of(tenantUuid),
                DtcCode.of("P0300"),
                FaultSeverity.CRITICAL,
                "Random/Multiple Cylinder Misfire Detected"
        );
        resolvedFault.resolve();

        when(faultQueryService.findById(faultId))
                .thenReturn(Optional.of(fault))
                .thenReturn(Optional.of(resolvedFault));

        mockMvc.perform(post("/api/v1/iot/faults/{id}/resolve", faultUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isResolved").value(true))
                .andExpect(jsonPath("$.resolvedAt").isNotEmpty());

        verify(faultCommandService).handle(any(ResolveVehicleFaultCommand.class));
    }

    @Test
    @DisplayName("Should return 403 when resolving fault belonging to different tenant")
    void shouldReturnForbiddenWhenFaultBelongsToDifferentTenant() throws Exception {
        UUID otherTenant = UUID.randomUUID();
        VehicleFault crossTenantFault = VehicleFault.detect(
                VehicleId.of(vehicleUuid),
                TenantId.of(otherTenant),
                DtcCode.of("P0300"),
                FaultSeverity.CRITICAL,
                "Cross-tenant test"
        );

        when(faultQueryService.findById(faultId)).thenReturn(Optional.of(crossTenantFault));

        mockMvc.perform(post("/api/v1/iot/faults/{id}/resolve", faultUuid))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access Denied"));
    }

    @Test
    @DisplayName("Should return 404 when resolving non-existent fault")
    void shouldReturnNotFoundWhenResolvingNonExistentFault() throws Exception {
        when(faultQueryService.findById(faultId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/iot/faults/{id}/resolve", faultUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Vehicle Fault Not Found"));
    }
}
