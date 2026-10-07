package com.andeva.atelier.platform.iot.application;

import com.andeva.atelier.platform.iot.application.commandservices.VehicleHealthReportCommandService;
import com.andeva.atelier.platform.iot.application.internal.commandservices.VehicleHealthReportCommandServiceImpl;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.AiInferenceDiagnosticPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.IoTSubscriptionQuotaPort;
import com.andeva.atelier.platform.iot.domain.model.commands.GenerateVehicleHealthReportCommand;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.interfaces.events.VehicleHealthReportGeneratedIntegrationEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link VehicleHealthReportCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class VehicleHealthReportCommandServiceTest {

    @Mock
    private IoTSubscriptionQuotaPort subscriptionQuotaPort;
    @Mock
    private AiInferenceDiagnosticPort aiInferenceDiagnosticPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private VehicleHealthReportCommandService service;

    private final TenantId tenantId = TenantId.generate();
    private final VehicleId vehicleId = VehicleId.generate();

    @BeforeEach
    void setUp() {
        service = new VehicleHealthReportCommandServiceImpl(
                subscriptionQuotaPort,
                aiInferenceDiagnosticPort,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should validate quota, generate diagnostic via AI port and publish integration event")
    void shouldSuccessfullyGenerateHealthReport() {
        GenerateVehicleHealthReportCommand command = new GenerateVehicleHealthReportCommand(
                tenantId, vehicleId, 30, true
        );

        VehicleHealthReportAiDto expectedReport = new VehicleHealthReportAiDto(
                92,
                "EXCELLENT",
                "Vehicle operating under optimal thermodynamic and electrical conditions.",
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        when(aiInferenceDiagnosticPort.generateVehicleDiagnostic(tenantId, vehicleId, 30, true))
                .thenReturn(expectedReport);

        VehicleHealthReportAiDto result = service.handle(command);

        assertThat(result).isNotNull();
        assertThat(result.healthScore()).isEqualTo(92);
        assertThat(result.overallCondition()).isEqualTo("EXCELLENT");

        verify(subscriptionQuotaPort).validateAiReportGenerationAllowed(tenantId, 0);

        ArgumentCaptor<VehicleHealthReportGeneratedIntegrationEvent> captor =
                ArgumentCaptor.forClass(VehicleHealthReportGeneratedIntegrationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        VehicleHealthReportGeneratedIntegrationEvent event = captor.getValue();
        assertThat(event.tenantId()).isEqualTo(tenantId.value());
        assertThat(event.vehicleId()).isEqualTo(vehicleId.value());
        assertThat(event.overallHealthScore()).isEqualTo(92);
        assertThat(event.healthTrafficLight()).isEqualTo("EXCELLENT");
    }
}
