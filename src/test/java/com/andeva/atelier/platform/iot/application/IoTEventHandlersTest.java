package com.andeva.atelier.platform.iot.application;

import com.andeva.atelier.platform.iot.application.internal.eventhandlers.*;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.FcmNotificationAclPort;
import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.events.CriticalEngineAnomalyDetectedEvent;
import com.andeva.atelier.platform.iot.domain.model.events.PredictiveAlertDispatchedEvent;
import com.andeva.atelier.platform.iot.domain.model.events.VehicleFaultDetectedEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.iot.domain.repositories.DeviceInstallationRepository;
import com.andeva.atelier.platform.iot.domain.repositories.PredictiveAlertRepository;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleFaultRepository;
import com.andeva.atelier.platform.iot.interfaces.events.*;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests covering all IoT Event Handlers and Transactional Outbox Publisher.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class IoTEventHandlersTest {

    private final TenantId tenantId = TenantId.generate();
    private final VehicleId vehicleId = VehicleId.generate();
    private final DeviceId deviceId = DeviceId.generate();

    @Nested
    @DisplayName("IoTTransactionalOutboxPublisher Tests")
    class OutboxPublisherTests {
        @Mock
        private OutboxMessageJpaRepository outboxRepository;

        @Test
        @DisplayName("Should persist predictive alert integration event to outbox")
        void shouldPersistPredictiveAlertEvent() {
            var publisher = new IoTTransactionalOutboxPublisher(outboxRepository, new ObjectMapper());
            var event = new PredictiveAlertGeneratedIntegrationEvent(
                    UUID.randomUUID(), tenantId.value(), vehicleId.value(),
                    "ENGINE_OVERHEATING_RISK", "DISPATCHED", 95.0, "Service needed", Instant.now()
            );

            publisher.on(event);

            ArgumentCaptor<OutboxMessagePersistenceEntity> captor =
                    ArgumentCaptor.forClass(OutboxMessagePersistenceEntity.class);
            verify(outboxRepository).save(captor.capture());

            OutboxMessagePersistenceEntity saved = captor.getValue();
            assertThat(saved.getAggregateType()).isEqualTo("IoTTelemetry");
            assertThat(saved.getEventType()).isEqualTo("PredictiveAlertGeneratedIntegrationEvent");
        }

        @Test
        @DisplayName("Should persist vehicle fault logged integration event to outbox")
        void shouldPersistVehicleFaultEvent() {
            var publisher = new IoTTransactionalOutboxPublisher(outboxRepository, new ObjectMapper());
            var event = new VehicleFaultLoggedIntegrationEvent(
                    UUID.randomUUID(), tenantId.value(), vehicleId.value(),
                    "P0300", "CRITICAL", "Cylinder misfire", Instant.now()
            );

            publisher.on(event);

            verify(outboxRepository).save(any(OutboxMessagePersistenceEntity.class));
        }

        @Test
        @DisplayName("Should persist vehicle health report generated event to outbox")
        void shouldPersistHealthReportEvent() {
            var publisher = new IoTTransactionalOutboxPublisher(outboxRepository, new ObjectMapper());
            var event = new VehicleHealthReportGeneratedIntegrationEvent(
                    tenantId.value(), vehicleId.value(), 90, "GREEN", 0, Instant.now()
            );

            publisher.on(event);

            verify(outboxRepository).save(any(OutboxMessagePersistenceEntity.class));
        }
    }

    @Nested
    @DisplayName("TelemetryDomainEventHandler Tests")
    class TelemetryDomainEventHandlerTests {
        @Mock
        private FcmNotificationAclPort fcmPort;

        @Test
        @DisplayName("Should dispatch high-priority push notification on critical engine anomaly")
        void shouldDispatchPushNotification() {
            var handler = new TelemetryDomainEventHandler(fcmPort);
            var event = CriticalEngineAnomalyDetectedEvent.of(
                    vehicleId, tenantId, AlertType.ENGINE_OVERHEATING_RISK,
                    ConfidenceScore.of(98.5), "Overheating!"
            );

            handler.on(event);

            verify(fcmPort).sendHighPriorityNotification(
                    eq(vehicleId),
                    contains("ENGINE_OVERHEATING_RISK"),
                    eq("Overheating!"),
                    anyMap()
            );
        }
    }

    @Nested
    @DisplayName("PredictiveAlertDomainEventHandler Tests")
    class PredictiveAlertDomainEventHandlerTests {
        @Mock
        private PredictiveAlertRepository alertRepository;
        @Mock
        private ApplicationEventPublisher eventPublisher;

        @Test
        @DisplayName("Should convert PredictiveAlertDispatchedEvent into integration event")
        void shouldConvertDispatchedEvent() {
            var handler = new PredictiveAlertDomainEventHandler(alertRepository, eventPublisher);
            AlertId alertId = AlertId.generate();
            PredictiveAlert alert = PredictiveAlert.create(
                    vehicleId, tenantId, Optional.empty(), AlertType.BATTERY_FAILURE_RISK,
                    ConfidenceScore.of(89.0), "Low resting voltage"
            );
            when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));

            handler.on(PredictiveAlertDispatchedEvent.of(alertId, vehicleId, tenantId, "fcm-msg-1"));

            ArgumentCaptor<PredictiveAlertGeneratedIntegrationEvent> captor =
                    ArgumentCaptor.forClass(PredictiveAlertGeneratedIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            PredictiveAlertGeneratedIntegrationEvent published = captor.getValue();
            assertThat(published.alertId()).isEqualTo(alert.getId().value());
            assertThat(published.vehicleId()).isEqualTo(vehicleId.value());
            assertThat(published.alertType()).isEqualTo("BATTERY_FAILURE_RISK");
        }
    }

    @Nested
    @DisplayName("VehicleFaultDomainEventHandler Tests")
    class VehicleFaultDomainEventHandlerTests {
        @Mock
        private VehicleFaultRepository faultRepository;
        @Mock
        private ApplicationEventPublisher eventPublisher;

        @Test
        @DisplayName("Should convert VehicleFaultDetectedEvent into integration event")
        void shouldConvertFaultDetectedEvent() {
            var handler = new VehicleFaultDomainEventHandler(faultRepository, eventPublisher);
            FaultId faultId = FaultId.generate();
            VehicleFault fault = VehicleFault.detect(
                    vehicleId, tenantId, DtcCode.of("P0300"), FaultSeverity.CRITICAL, "Cylinder misfire"
            );
            when(faultRepository.findById(faultId)).thenReturn(Optional.of(fault));

            handler.on(VehicleFaultDetectedEvent.of(faultId, vehicleId, tenantId, DtcCode.of("P0300"), FaultSeverity.CRITICAL));

            ArgumentCaptor<VehicleFaultLoggedIntegrationEvent> captor =
                    ArgumentCaptor.forClass(VehicleFaultLoggedIntegrationEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            VehicleFaultLoggedIntegrationEvent published = captor.getValue();
            assertThat(published.dtcCode()).isEqualTo("P0300");
            assertThat(published.severity()).isEqualTo("CRITICAL");
        }
    }

    @Nested
    @DisplayName("VehicleLifecycleIntegrationEventHandler Tests")
    class VehicleLifecycleIntegrationEventHandlerTests {
        @Mock
        private DeviceInstallationRepository installationRepository;
        @Mock
        private VehicleFaultRepository faultRepository;

        @Test
        @DisplayName("Should auto-uninstall device when vehicle is decommissioned")
        void shouldAutoUninstallOnDecommission() {
            var handler = new VehicleLifecycleIntegrationEventHandler(installationRepository, faultRepository);
            DeviceInstallation installation = DeviceInstallation.install(deviceId, vehicleId, tenantId, 10000);
            when(installationRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(installation));

            handler.on(new VehicleDecommissionedIntegrationEvent(tenantId.value(), vehicleId.value(), Instant.now()));

            assertThat(installation.isActive()).isFalse();
            verify(installationRepository).save(installation);
        }

        @Test
        @DisplayName("Should auto-uninstall device when vehicle ownership is transferred")
        void shouldAutoUninstallOnOwnershipTransfer() {
            var handler = new VehicleLifecycleIntegrationEventHandler(installationRepository, faultRepository);
            DeviceInstallation installation = DeviceInstallation.install(deviceId, vehicleId, tenantId, 10000);
            when(installationRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(installation));

            handler.on(new VehicleOwnershipTransferredIntegrationEvent(
                    tenantId.value(), vehicleId.value(), UUID.randomUUID(), UUID.randomUUID(), Instant.now()
            ));

            assertThat(installation.isActive()).isFalse();
            verify(installationRepository).save(installation);
        }

        @Test
        @DisplayName("Should auto-resolve matching DTC faults when work order is completed")
        void shouldAutoResolveMatchingFaultsOnWorkOrderCompleted() {
            var handler = new VehicleLifecycleIntegrationEventHandler(installationRepository, faultRepository);
            VehicleFault faultP0300 = VehicleFault.detect(vehicleId, tenantId, DtcCode.of("P0300"), FaultSeverity.CRITICAL, "Misfire");
            VehicleFault faultP0117 = VehicleFault.detect(vehicleId, tenantId, DtcCode.of("P0117"), FaultSeverity.CRITICAL, "Temp");
            when(faultRepository.findActiveByVehicleId(vehicleId)).thenReturn(List.of(faultP0300, faultP0117));

            handler.on(new WorkOrderCompletedIntegrationEvent(
                    tenantId.value(), vehicleId.value(), UUID.randomUUID(), List.of("P0300"), Instant.now()
            ));

            assertThat(faultP0300.isResolved()).isTrue();
            assertThat(faultP0117.isResolved()).isFalse();
            verify(faultRepository).save(faultP0300);
            verify(faultRepository, never()).save(faultP0117);
        }

        @Test
        @DisplayName("Should auto-uninstall device when real CRM vehicle ownership is transferred")
        void shouldAutoUninstallOnRealCrmOwnershipTransfer() {
            var handler = new VehicleLifecycleIntegrationEventHandler(installationRepository, faultRepository);
            DeviceInstallation installation = DeviceInstallation.install(deviceId, vehicleId, tenantId, 12000);
            when(installationRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(installation));

            handler.on(new com.andeva.atelier.platform.crm.interfaces.events.VehicleOwnershipTransferredIntegrationEvent(
                    vehicleId.value(), UUID.randomUUID(), UUID.randomUUID(), java.time.LocalDate.now(), Instant.now()
            ));

            assertThat(installation.isActive()).isFalse();
            verify(installationRepository).save(installation);
        }

        @Test
        @DisplayName("Should auto-resolve all active faults when real Operations work order is completed")
        void shouldAutoResolveAllActiveFaultsOnRealOperationsWorkOrderCompleted() {
            var handler = new VehicleLifecycleIntegrationEventHandler(installationRepository, faultRepository);
            VehicleFault faultP0300 = VehicleFault.detect(vehicleId, tenantId, DtcCode.of("P0300"), FaultSeverity.CRITICAL, "Misfire");
            VehicleFault faultP0117 = VehicleFault.detect(vehicleId, tenantId, DtcCode.of("P0117"), FaultSeverity.CRITICAL, "Temp");
            when(faultRepository.findActiveByVehicleId(vehicleId)).thenReturn(List.of(faultP0300, faultP0117));

            handler.on(new com.andeva.atelier.platform.operations.interfaces.events.WorkOrderCompletedIntegrationEvent(
                    UUID.randomUUID(), tenantId.value(), vehicleId.value(), java.math.BigDecimal.valueOf(250.00), "PEN", Instant.now()
            ));

            assertThat(faultP0300.isResolved()).isTrue();
            assertThat(faultP0117.isResolved()).isTrue();
            verify(faultRepository).save(faultP0300);
            verify(faultRepository).save(faultP0117);
        }
    }
}
