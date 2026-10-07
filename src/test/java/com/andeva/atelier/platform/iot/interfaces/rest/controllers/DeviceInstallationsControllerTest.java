package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.DeviceInstallationCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.DeviceInstallationQueryService;
import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.commands.InstallDeviceOnVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.UninstallDeviceFromVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.iot.domain.model.queries.GetDeviceByVehicleIdQuery;
import com.andeva.atelier.platform.iot.interfaces.rest.advice.IoTExceptionHandler;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.InstallDeviceRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.UninstallDeviceRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.DeviceInstallationResourceAssembler;
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
 * Unit tests for {@link DeviceInstallationsController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceInstallationsController Unit Tests")
class DeviceInstallationsControllerTest {

    @Mock
    private DeviceInstallationCommandService installationCommandService;

    @Mock
    private DeviceInstallationQueryService installationQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();
    private final UUID vehicleUuid = UUID.randomUUID();
    private final UUID deviceUuid = UUID.randomUUID();

    private TenantId tenantId;
    private VehicleId vehicleId;
    private DeviceId deviceId;
    private DeviceInstallation installation;
    private InstallationId installationId;

    @BeforeEach
    void setUp() {
        tenantId = new TenantId(tenantUuid);
        vehicleId = new VehicleId(vehicleUuid);
        deviceId = new DeviceId(deviceUuid);

        DeviceInstallationResourceAssembler assembler = new DeviceInstallationResourceAssembler();
        DeviceInstallationsController controller = new DeviceInstallationsController(
                installationCommandService,
                installationQueryService,
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

        installation = DeviceInstallation.install(
                deviceId,
                vehicleId,
                tenantId,
                45000
        );
        installationId = installation.getId();
    }

    @Test
    @DisplayName("POST /api/v1/iot/installations/install binds device to vehicle and returns 201")
    void installDeviceReturns201() throws Exception {
        InstallDeviceRequest request = new InstallDeviceRequest(
                deviceUuid,
                vehicleUuid,
                45000
        );

        when(installationCommandService.handle(any(InstallDeviceOnVehicleCommand.class))).thenReturn(installationId);
        when(installationQueryService.findById(installationId)).thenReturn(Optional.of(installation));

        mockMvc.perform(post("/api/v1/iot/installations/install")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/iot/installations/" + installationId.value()))
                .andExpect(jsonPath("$.id").value(installationId.value().toString()))
                .andExpect(jsonPath("$.deviceId").value(deviceUuid.toString()))
                .andExpect(jsonPath("$.vehicleId").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$.initialOdometerKm").value(45000))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/iot/installations/{id}/uninstall unbinds device and returns 200")
    void uninstallDeviceReturns200() throws Exception {
        UninstallDeviceRequest request = new UninstallDeviceRequest(45100, null);

        when(installationQueryService.findById(installationId)).thenReturn(Optional.of(installation));

        mockMvc.perform(post("/api/v1/iot/installations/" + installationId.value() + "/uninstall")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(installationId.value().toString()));

        verify(installationCommandService).handle(any(UninstallDeviceFromVehicleCommand.class));
    }

    @Test
    @DisplayName("GET /api/v1/iot/installations/vehicle/{vehicleId}/active returns active installation")
    void getActiveInstallationReturns200() throws Exception {
        when(installationQueryService.handle(any(GetDeviceByVehicleIdQuery.class))).thenReturn(Optional.of(installation));

        mockMvc.perform(get("/api/v1/iot/installations/vehicle/" + vehicleUuid + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(installationId.value().toString()))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/iot/installations/vehicle/{vehicleId}/active returns 404 when none active")
    void getActiveInstallationNotFoundReturns404() throws Exception {
        when(installationQueryService.handle(any(GetDeviceByVehicleIdQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/iot/installations/vehicle/" + vehicleUuid + "/active"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://api.atelier.andeva.com/errors/installation-not-found"));
    }
}
