package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.Obd2DeviceCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.Obd2DeviceQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.DeviceNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.Obd2Device;
import com.andeva.atelier.platform.iot.domain.model.commands.RegisterObd2DeviceCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.UpdateDeviceStatusCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.queries.GetDeviceByIdQuery;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.iot.interfaces.rest.advice.IoTExceptionHandler;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.RegisterDeviceRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.UpdateDeviceStatusRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.Obd2DeviceResourceAssembler;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link Obd2DevicesController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Obd2DevicesController Unit Tests")
class Obd2DevicesControllerTest {

    @Mock
    private Obd2DeviceCommandService deviceCommandService;

    @Mock
    private Obd2DeviceQueryService deviceQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();
    private TenantId tenantId;
    private Obd2Device device;
    private DeviceId deviceId;

    @BeforeEach
    void setUp() {
        tenantId = new TenantId(tenantUuid);
        Obd2DeviceResourceAssembler assembler = new Obd2DeviceResourceAssembler();
        Obd2DevicesController controller = new Obd2DevicesController(
                deviceCommandService,
                deviceQueryService,
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

        device = Obd2Device.register(
                tenantId,
                DeviceIdentifier.of("00:1B:44:11:3A:B7"),
                ConnectionType.BLUETOOTH_BLE,
                "ELM327-v2.2-Pro",
                "v1.4.2"
        );
        deviceId = device.getId();
    }

    @Test
    @DisplayName("POST /api/v1/iot/devices registers device and returns 201")
    void registerDeviceReturns201() throws Exception {
        RegisterDeviceRequest request = new RegisterDeviceRequest(
                "00:1B:44:11:3A:B7",
                "BLUETOOTH_BLE",
                "ELM327-v2.2-Pro",
                "v1.4.2"
        );

        when(deviceCommandService.handle(any(RegisterObd2DeviceCommand.class))).thenReturn(deviceId);
        when(deviceQueryService.handle(any(GetDeviceByIdQuery.class))).thenReturn(Optional.of(device));

        mockMvc.perform(post("/api/v1/iot/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/iot/devices/" + deviceId.value()))
                .andExpect(jsonPath("$.id").value(deviceId.value().toString()))
                .andExpect(jsonPath("$.deviceIdentifier").value("00:1B:44:11:3A:B7"))
                .andExpect(jsonPath("$.connectionType").value("BLUETOOTH_BLE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/v1/iot/devices/{id} returns 200 when device belongs to tenant")
    void getDeviceByIdReturns200() throws Exception {
        when(deviceQueryService.handle(any(GetDeviceByIdQuery.class))).thenReturn(Optional.of(device));

        mockMvc.perform(get("/api/v1/iot/devices/" + deviceId.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deviceId.value().toString()))
                .andExpect(jsonPath("$.deviceIdentifier").value("00:1B:44:11:3A:B7"));
    }

    @Test
    @DisplayName("GET /api/v1/iot/devices/{id} returns 404 when device not found")
    void getDeviceByIdNotFoundReturns404() throws Exception {
        UUID unknown = UUID.randomUUID();
        when(deviceQueryService.handle(any(GetDeviceByIdQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/iot/devices/" + unknown))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://api.atelier.andeva.com/errors/device-not-found"));
    }

    @Test
    @DisplayName("GET /api/v1/iot/devices returns list of devices for tenant")
    void listDevicesReturns200() throws Exception {
        when(deviceQueryService.getDevicesByTenant(tenantId)).thenReturn(List.of(device));

        mockMvc.perform(get("/api/v1/iot/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].deviceIdentifier").value("00:1B:44:11:3A:B7"));
    }

    @Test
    @DisplayName("PATCH /api/v1/iot/devices/{id}/status updates status and returns 200")
    void updateDeviceStatusReturns200() throws Exception {
        UpdateDeviceStatusRequest request = new UpdateDeviceStatusRequest("INACTIVE");

        when(deviceQueryService.handle(any(GetDeviceByIdQuery.class))).thenReturn(Optional.of(device));

        mockMvc.perform(patch("/api/v1/iot/devices/" + deviceId.value() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deviceId.value().toString()));

        verify(deviceCommandService).handle(any(UpdateDeviceStatusCommand.class));
    }
}
