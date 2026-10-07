package com.andeva.atelier.platform.iot.application.internal.eventhandlers;

import com.andeva.atelier.platform.iot.application.internal.outbound.acl.FcmNotificationAclPort;
import com.andeva.atelier.platform.iot.domain.model.events.CriticalEngineAnomalyDetectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.Objects;

/**
 * Domain Event Handler reacting to critical engine anomalies after transaction commit.
 * Dispatches high-priority push notifications to driver and workshop reception channels.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class TelemetryDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TelemetryDomainEventHandler.class);

    private final FcmNotificationAclPort fcmNotificationAclPort;

    public TelemetryDomainEventHandler(FcmNotificationAclPort fcmNotificationAclPort) {
        this.fcmNotificationAclPort = Objects.requireNonNull(fcmNotificationAclPort, "FcmNotificationAclPort cannot be null");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CriticalEngineAnomalyDetectedEvent event) {
        log.warn("Handling critical engine anomaly domain event for vehicle: {} - {}",
                event.vehicleId(), event.type());

        try {
            fcmNotificationAclPort.sendHighPriorityNotification(
                    event.vehicleId(),
                    "Critical Telemetry Alert: " + event.type().name(),
                    event.message(),
                    Map.of(
                            "alertType", event.type().name(),
                            "severity", "CRITICAL",
                            "score", String.valueOf(event.score().percentage())
                    )
            );
        } catch (Exception e) {
            log.error("Failed to dispatch push notification for critical anomaly on vehicle {}", event.vehicleId(), e);
        }
    }
}
