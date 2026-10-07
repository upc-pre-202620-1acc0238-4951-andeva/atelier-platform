package com.andeva.atelier.platform.iot.application;

import com.andeva.atelier.platform.iot.application.commandservices.PredictiveAlertCommandService;
import com.andeva.atelier.platform.iot.application.internal.commandservices.PredictiveAlertCommandServiceImpl;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.domain.exceptions.PredictiveAlertNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.commands.AcknowledgePredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.ConvertAlertToAppointmentCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.DismissPredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.GeneratePredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.events.PredictiveAlertAcknowledgedEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.iot.domain.repositories.PredictiveAlertRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PredictiveAlertCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class PredictiveAlertCommandServiceTest {

    @Mock
    private PredictiveAlertRepository predictiveAlertRepository;
    @Mock
    private CrmFleetAclPort crmFleetAclPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PredictiveAlertCommandService service;

    private final TenantId tenantId = TenantId.generate();
    private final VehicleId vehicleId = VehicleId.generate();
    private final ServiceId serviceId = ServiceId.generate();

    @BeforeEach
    void setUp() {
        service = new PredictiveAlertCommandServiceImpl(
                predictiveAlertRepository,
                crmFleetAclPort,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should successfully generate predictive alert and save to repository")
    void shouldSuccessfullyGeneratePredictiveAlert() {
        GeneratePredictiveAlertCommand command = new GeneratePredictiveAlertCommand(
                vehicleId,
                tenantId,
                Optional.of(serviceId),
                AlertType.ENGINE_OVERHEATING_RISK,
                ConfidenceScore.of(92.5),
                "Thermostat failure risk detected"
        );

        AlertId alertId = service.handle(command);
        assertThat(alertId).isNotNull();

        ArgumentCaptor<PredictiveAlert> captor = ArgumentCaptor.forClass(PredictiveAlert.class);
        verify(predictiveAlertRepository).save(captor.capture());

        PredictiveAlert saved = captor.getValue();
        assertThat(saved.getVehicleId()).isEqualTo(vehicleId);
        assertThat(saved.getTenantId()).isEqualTo(tenantId);
        assertThat(saved.getAlertType()).isEqualTo(AlertType.ENGINE_OVERHEATING_RISK);
        assertThat(saved.getStatus()).isEqualTo(AlertStatus.DISPATCHED);
    }

    @Test
    @DisplayName("Should acknowledge alert and emit PredictiveAlertAcknowledgedEvent")
    void shouldAcknowledgeAlert() {
        AlertId alertId = AlertId.generate();
        PredictiveAlert alert = PredictiveAlert.create(
                vehicleId, tenantId, Optional.empty(), AlertType.BATTERY_FAILURE_RISK,
                ConfidenceScore.of(88.0), "Low resting voltage"
        );
        when(predictiveAlertRepository.findById(alertId)).thenReturn(Optional.of(alert));

        AcknowledgePredictiveAlertCommand command = new AcknowledgePredictiveAlertCommand(alertId);
        service.handle(command);

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        verify(predictiveAlertRepository).save(alert);
        verify(eventPublisher, atLeastOnce()).publishEvent(any(PredictiveAlertAcknowledgedEvent.class));
    }

    @Test
    @DisplayName("Should dismiss alert")
    void shouldDismissAlert() {
        AlertId alertId = AlertId.generate();
        PredictiveAlert alert = PredictiveAlert.create(
                vehicleId, tenantId, Optional.empty(), AlertType.CATALYTIC_SYSTEM_DEGRADATION,
                ConfidenceScore.of(86.0), "Catalyst efficiency below threshold"
        );
        when(predictiveAlertRepository.findById(alertId)).thenReturn(Optional.of(alert));

        DismissPredictiveAlertCommand command = new DismissPredictiveAlertCommand(alertId);
        service.handle(command);

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.DISMISSED);
        verify(predictiveAlertRepository).save(alert);
    }

    @Test
    @DisplayName("Should convert alert to appointment via CRM port and mark alert resolved")
    void shouldConvertAlertToAppointment() {
        AlertId alertId = AlertId.generate();
        PredictiveAlert alert = PredictiveAlert.create(
                vehicleId, tenantId, Optional.empty(), AlertType.CYLINDER_MISFIRE_HAZARD,
                ConfidenceScore.of(95.0), "Spark plug failure predicted"
        );
        when(predictiveAlertRepository.findById(alertId)).thenReturn(Optional.of(alert));

        UUID expectedAppointmentId = UUID.randomUUID();
        when(crmFleetAclPort.convertAlertToAppointment(any(), any(), any(), any(), any()))
                .thenReturn(expectedAppointmentId);

        ConvertAlertToAppointmentCommand command = new ConvertAlertToAppointmentCommand(alertId);
        UUID appointmentId = service.handle(command);

        assertThat(appointmentId).isEqualTo(expectedAppointmentId);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        verify(predictiveAlertRepository).save(alert);
    }

    @Test
    @DisplayName("Should throw PredictiveAlertNotFoundException when alert does not exist")
    void shouldThrowWhenAlertNotFound() {
        AlertId alertId = AlertId.generate();
        when(predictiveAlertRepository.findById(alertId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new AcknowledgePredictiveAlertCommand(alertId)))
                .isInstanceOf(PredictiveAlertNotFoundException.class);
    }
}
