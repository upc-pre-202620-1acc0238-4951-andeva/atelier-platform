package com.andeva.atelier.platform.iot.application.internal.eventhandlers;

import com.andeva.atelier.platform.iot.interfaces.events.PredictiveAlertGeneratedIntegrationEvent;
import com.andeva.atelier.platform.iot.interfaces.events.VehicleFaultLoggedIntegrationEvent;
import com.andeva.atelier.platform.iot.interfaces.events.VehicleHealthReportGeneratedIntegrationEvent;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

/**
 * Transactional Outbox Publisher for IoT Telemetry & Predictive Maintenance integration events.
 * Atomically records integration events in the PostgreSQL {@code outbox_messages} table
 * within the active transaction to guarantee At-Least-Once Delivery.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class IoTTransactionalOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(IoTTransactionalOutboxPublisher.class);
    private static final String AGGREGATE_TYPE = "IoTTelemetry";

    private final OutboxMessageJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public IoTTransactionalOutboxPublisher(
            OutboxMessageJpaRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "OutboxMessageJpaRepository cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper cannot be null");
        this.objectMapper.findAndRegisterModules();
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(PredictiveAlertGeneratedIntegrationEvent event) {
        persistOutboxMessage(event.alertId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VehicleFaultLoggedIntegrationEvent event) {
        persistOutboxMessage(event.faultId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VehicleHealthReportGeneratedIntegrationEvent event) {
        persistOutboxMessage(event.vehicleId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    private void persistOutboxMessage(
            String aggregateId,
            String eventType,
            Object event,
            Instant occurredOn
    ) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxMessagePersistenceEntity outboxEntity = OutboxMessagePersistenceEntity.pendingOf(
                    AGGREGATE_TYPE,
                    aggregateId,
                    eventType,
                    payload,
                    occurredOn != null ? occurredOn : Instant.now()
            );
            outboxRepository.save(outboxEntity);
            log.debug("IoT integration event persisted to Outbox: type={}, aggregateId={}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize IoT integration event for Outbox: {}", event, e);
            throw new IllegalStateException("Failed to serialize IoT integration event for outbox", e);
        }
    }
}
