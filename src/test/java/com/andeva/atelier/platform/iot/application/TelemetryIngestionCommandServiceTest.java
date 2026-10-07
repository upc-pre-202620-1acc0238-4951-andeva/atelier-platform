package com.andeva.atelier.platform.iot.application;

import com.andeva.atelier.platform.iot.application.commandservices.TelemetryIngestionCommandService;
import com.andeva.atelier.platform.iot.application.internal.commandservices.TelemetryIngestionCommandServiceImpl;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.FcmNotificationAclPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.OperationsAclPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.TimescaleBatchJdbcClientPort;
import com.andeva.atelier.platform.iot.domain.exceptions.InstallationNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.DeviceInstallation;
import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.commands.IngestTelemetryBatchCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.events.CriticalEngineAnomalyDetectedEvent;
import com.andeva.atelier.platform.iot.domain.model.events.TelemetryBatchIngestedEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.*;
import com.andeva.atelier.platform.iot.domain.repositories.DeviceInstallationRepository;
import com.andeva.atelier.platform.iot.domain.repositories.PredictiveAlertRepository;
import com.andeva.atelier.platform.iot.domain.repositories.TelemetryLogRepository;
import com.andeva.atelier.platform.iot.domain.services.PredictiveAnomalyDetectionEngine;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TelemetryIngestionCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class TelemetryIngestionCommandServiceTest {

    @Mock
    private DeviceInstallationRepository deviceInstallationRepository;
    @Mock
    private TelemetryLogRepository telemetryLogRepository;
    @Mock
    private TimescaleBatchJdbcClientPort timescaleBatchJdbcClientPort;
    @Mock
    private OperationsAclPort operationsAclPort;
    @Mock
    private FcmNotificationAclPort fcmNotificationAclPort;
    @Mock
    private PredictiveAlertRepository predictiveAlertRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PredictiveAnomalyDetectionEngine engine;
    private TelemetryIngestionCommandService service;

    private final TenantId tenantId = TenantId.generate();
    private final VehicleId vehicleId = VehicleId.generate();
    private final DeviceId deviceId = DeviceId.generate();

    @BeforeEach
    void setUp() {
        engine = new PredictiveAnomalyDetectionEngine();
        service = new TelemetryIngestionCommandServiceImpl(
                deviceInstallationRepository,
                telemetryLogRepository,
                timescaleBatchJdbcClientPort,
                engine,
                operationsAclPort,
                fcmNotificationAclPort,
                predictiveAlertRepository,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should reject ingestion when vehicle has no active installation")
    void shouldRejectIngestionWhenVehicleHasNoActiveInstallation() {
        when(deviceInstallationRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.empty());

        IngestTelemetryBatchCommand command = new IngestTelemetryBatchCommand(
                vehicleId,
                tenantId,
                List.of(createRecord(90.0, 13.5, 2000, 60.0))
        );

        assertThatThrownBy(() -> service.handle(command))
                .isInstanceOf(InstallationNotFoundException.class);

        verifyNoInteractions(timescaleBatchJdbcClientPort);
    }

    @Test
    @DisplayName("Should do nothing when batch readings is empty")
    void shouldDoNothingWhenBatchReadingsIsEmpty() {
        DeviceInstallation installation = DeviceInstallation.install(deviceId, vehicleId, tenantId, 10000);
        when(deviceInstallationRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(installation));

        IngestTelemetryBatchCommand command = new IngestTelemetryBatchCommand(
                vehicleId,
                tenantId,
                List.of()
        );

        service.handle(command);

        verifyNoInteractions(timescaleBatchJdbcClientPort);
        verifyNoInteractions(predictiveAlertRepository);
    }

    @Test
    @DisplayName("Should successfully ingest normal telemetry batch and publish TelemetryBatchIngestedEvent")
    void shouldSuccessfullyIngestNormalTelemetryBatch() {
        DeviceInstallation installation = DeviceInstallation.install(deviceId, vehicleId, tenantId, 10000);
        when(deviceInstallationRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(installation));

        TelemetryRecord record1 = createRecord(88.0, 14.1, 2200, 70.0);
        TelemetryRecord record2 = createRecord(90.0, 14.0, 2400, 75.0);

        IngestTelemetryBatchCommand command = new IngestTelemetryBatchCommand(
                vehicleId,
                tenantId,
                List.of(record1, record2)
        );

        service.handle(command);

        verify(timescaleBatchJdbcClientPort).executeBatchInsert(command.readings());
        verifyNoInteractions(predictiveAlertRepository);
        verifyNoInteractions(fcmNotificationAclPort);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        assertThat(eventCaptor.getValue()).isInstanceOf(TelemetryBatchIngestedEvent.class);
        TelemetryBatchIngestedEvent event = (TelemetryBatchIngestedEvent) eventCaptor.getValue();
        assertThat(event.vehicleId()).isEqualTo(vehicleId);
        assertThat(event.recordsCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should detect critical engine overheating, create alert and dispatch FCM notification")
    void shouldDetectCriticalEngineOverheatingAndDispatchAlert() {
        DeviceInstallation installation = DeviceInstallation.install(deviceId, vehicleId, tenantId, 10000);
        when(deviceInstallationRepository.findActiveByVehicleId(vehicleId)).thenReturn(Optional.of(installation));

        UUID serviceCatalogId = UUID.randomUUID();
        when(operationsAclPort.getAvailableWorkshopServices(tenantId)).thenReturn(List.of(
                new OperationsAclPort.WorkshopServiceCatalogItemDto(serviceCatalogId, "SRV-COOL", "Cooling System Service", "Cooling")
        ));
        when(fcmNotificationAclPort.sendHighPriorityNotification(eq(vehicleId), anyString(), anyString(), anyMap()))
                .thenReturn("fcm-msg-12345");

        TelemetryRecord overheatingRecord = createRecord(118.0, 13.8, 3000, 80.0);
        IngestTelemetryBatchCommand command = new IngestTelemetryBatchCommand(
                vehicleId,
                tenantId,
                List.of(overheatingRecord)
        );

        service.handle(command);

        verify(timescaleBatchJdbcClientPort).executeBatchInsert(command.readings());

        ArgumentCaptor<PredictiveAlert> alertCaptor = ArgumentCaptor.forClass(PredictiveAlert.class);
        verify(predictiveAlertRepository).save(alertCaptor.capture());

        PredictiveAlert alert = alertCaptor.getValue();
        assertThat(alert.getVehicleId()).isEqualTo(vehicleId);
        assertThat(alert.getTenantId()).isEqualTo(tenantId);
        assertThat(alert.getAlertType()).isEqualTo(AlertType.ENGINE_OVERHEATING_RISK);
        assertThat(alert.getRecommendedServiceId()).contains(ServiceId.of(serviceCatalogId));

        verify(fcmNotificationAclPort).sendHighPriorityNotification(
                eq(vehicleId),
                eq("Critical Telemetry Anomaly Detected"),
                anyString(),
                anyMap()
        );

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeast(2)).publishEvent(eventCaptor.capture());

        List<Object> events = eventCaptor.getAllValues();
        assertThat(events).anyMatch(e -> e instanceof CriticalEngineAnomalyDetectedEvent);
        assertThat(events).anyMatch(e -> e instanceof TelemetryBatchIngestedEvent);
    }

    private TelemetryRecord createRecord(double tempCelsius, double batteryVolts, int rpm, double speedKmh) {
        return TelemetryRecord.of(
                Instant.now(),
                vehicleId,
                tenantId,
                null,
                VehicleSpeed.of(speedKmh),
                EngineTemperature.of(tempCelsius),
                EngineRpm.of(rpm),
                FuelLevel.of(65.0),
                BatteryVoltage.of(batteryVolts)
        );
    }
}
