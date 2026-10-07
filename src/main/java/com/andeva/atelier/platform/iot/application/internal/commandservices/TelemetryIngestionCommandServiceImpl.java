package com.andeva.atelier.platform.iot.application.internal.commandservices;

import com.andeva.atelier.platform.iot.application.commandservices.TelemetryIngestionCommandService;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.FcmNotificationAclPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.OperationsAclPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.TimescaleBatchJdbcClientPort;
import com.andeva.atelier.platform.iot.domain.exceptions.InstallationNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.commands.IngestTelemetryBatchCommand;
import com.andeva.atelier.platform.iot.domain.model.events.CriticalEngineAnomalyDetectedEvent;
import com.andeva.atelier.platform.iot.domain.model.events.TelemetryBatchIngestedEvent;
import com.andeva.atelier.platform.iot.domain.repositories.DeviceInstallationRepository;
import com.andeva.atelier.platform.iot.domain.repositories.PredictiveAlertRepository;
import com.andeva.atelier.platform.iot.domain.repositories.TelemetryLogRepository;
import com.andeva.atelier.platform.iot.domain.services.PredictiveAnomalyDetectionEngine;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Command Service implementation for ingesting telemetry batches into TimescaleDB
 * and performing real-time mathematical anomaly detection.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class TelemetryIngestionCommandServiceImpl implements TelemetryIngestionCommandService {

    private final DeviceInstallationRepository deviceInstallationRepository;
    private final TelemetryLogRepository telemetryLogRepository;
    private final TimescaleBatchJdbcClientPort timescaleBatchJdbcClientPort;
    private final PredictiveAnomalyDetectionEngine predictiveAnomalyDetectionEngine;
    private final OperationsAclPort operationsAclPort;
    private final FcmNotificationAclPort fcmNotificationAclPort;
    private final PredictiveAlertRepository predictiveAlertRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TelemetryIngestionCommandServiceImpl(
            DeviceInstallationRepository deviceInstallationRepository,
            TelemetryLogRepository telemetryLogRepository,
            TimescaleBatchJdbcClientPort timescaleBatchJdbcClientPort,
            PredictiveAnomalyDetectionEngine predictiveAnomalyDetectionEngine,
            OperationsAclPort operationsAclPort,
            FcmNotificationAclPort fcmNotificationAclPort,
            PredictiveAlertRepository predictiveAlertRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.deviceInstallationRepository = Objects.requireNonNull(deviceInstallationRepository, "DeviceInstallationRepository cannot be null");
        this.telemetryLogRepository = Objects.requireNonNull(telemetryLogRepository, "TelemetryLogRepository cannot be null");
        this.timescaleBatchJdbcClientPort = Objects.requireNonNull(timescaleBatchJdbcClientPort, "TimescaleBatchJdbcClientPort cannot be null");
        this.predictiveAnomalyDetectionEngine = Objects.requireNonNull(predictiveAnomalyDetectionEngine, "PredictiveAnomalyDetectionEngine cannot be null");
        this.operationsAclPort = Objects.requireNonNull(operationsAclPort, "OperationsAclPort cannot be null");
        this.fcmNotificationAclPort = Objects.requireNonNull(fcmNotificationAclPort, "FcmNotificationAclPort cannot be null");
        this.predictiveAlertRepository = Objects.requireNonNull(predictiveAlertRepository, "PredictiveAlertRepository cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @Override
    public void handle(IngestTelemetryBatchCommand command) {
        Objects.requireNonNull(command, "IngestTelemetryBatchCommand cannot be null");

        // 1. Validate active device installation and assert tenant ownership
        var activeInstallation = deviceInstallationRepository.findActiveByVehicleId(command.vehicleId())
                .orElseThrow(() -> new InstallationNotFoundException(command.vehicleId()));

        if (!activeInstallation.getTenantId().equals(command.tenantId())) {
            throw new IllegalStateException(
                    String.format("Active installation on vehicle %s does not belong to tenant %s",
                            command.vehicleId(), command.tenantId())
            );
        }

        if (command.readings().isEmpty()) {
            return;
        }

        // 2. High-speed bulk JDBC persistence into TimescaleDB hypertable
        timescaleBatchJdbcClientPort.executeBatchInsert(command.readings());

        // 3. Real-time inference on the latest sensory record
        TelemetryRecord latestReading = command.readings().stream()
                .max(Comparator.comparing(TelemetryRecord::timestamp))
                .orElse(command.readings().get(0));

        var evaluationOpt = predictiveAnomalyDetectionEngine.evaluateTelemetryRecord(latestReading);

        // 4. Predictive alert generation & push notification dispatch
        if (evaluationOpt.isPresent()) {
            var evaluation = evaluationOpt.get();
            Optional<ServiceId> recommendedServiceId = Optional.empty();
            List<OperationsAclPort.WorkshopServiceCatalogItemDto> services =
                    operationsAclPort.getAvailableWorkshopServices(command.tenantId());
            if (services != null && !services.isEmpty()) {
                recommendedServiceId = Optional.of(ServiceId.of(services.get(0).serviceId()));
            }

            PredictiveAlert alert = PredictiveAlert.create(
                    command.vehicleId(),
                    command.tenantId(),
                    recommendedServiceId,
                    evaluation.alertType(),
                    evaluation.confidenceScore(),
                    evaluation.diagnosticMessage()
            );

            String fcmMessageId = fcmNotificationAclPort.sendHighPriorityNotification(
                    command.vehicleId(),
                    "Critical Telemetry Anomaly Detected",
                    evaluation.diagnosticMessage(),
                    Map.of(
                            "alertType", evaluation.alertType().name(),
                            "severity", "CRITICAL",
                            "score", String.valueOf(evaluation.confidenceScore().percentage())
                    )
            );

            if (fcmMessageId != null) {
                alert.recordDispatch(fcmMessageId);
            }

            predictiveAlertRepository.save(alert);

            eventPublisher.publishEvent(CriticalEngineAnomalyDetectedEvent.of(
                    command.vehicleId(),
                    command.tenantId(),
                    evaluation.alertType(),
                    evaluation.confidenceScore(),
                    evaluation.diagnosticMessage()
            ));

            alert.domainEvents().forEach(eventPublisher::publishEvent);
        }

        // 5. Ingestion completed domain event
        eventPublisher.publishEvent(TelemetryBatchIngestedEvent.of(
                command.vehicleId(),
                command.tenantId(),
                command.readings().size(),
                latestReading.timestamp()
        ));
    }
}
