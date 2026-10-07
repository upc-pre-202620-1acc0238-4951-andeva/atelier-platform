package com.andeva.atelier.platform.iot.application.internal.eventhandlers;

import com.andeva.atelier.platform.iot.domain.model.events.PredictiveAlertDispatchedEvent;
import com.andeva.atelier.platform.iot.domain.repositories.PredictiveAlertRepository;
import com.andeva.atelier.platform.iot.interfaces.events.PredictiveAlertGeneratedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Domain Event Handler reacting to dispatched predictive maintenance alerts.
 * Emits {@link PredictiveAlertGeneratedIntegrationEvent} to integrate with CRM and MRO contexts.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class PredictiveAlertDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PredictiveAlertDomainEventHandler.class);

    private final PredictiveAlertRepository predictiveAlertRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PredictiveAlertDomainEventHandler(
            PredictiveAlertRepository predictiveAlertRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.predictiveAlertRepository = Objects.requireNonNull(predictiveAlertRepository, "PredictiveAlertRepository cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @EventListener
    public void on(PredictiveAlertDispatchedEvent event) {
        log.info("Processing PredictiveAlertDispatchedEvent for alertId: {}", event.alertId());

        predictiveAlertRepository.findById(event.alertId()).ifPresent(alert -> {
            PredictiveAlertGeneratedIntegrationEvent integrationEvent = new PredictiveAlertGeneratedIntegrationEvent(
                    alert.getId().value(),
                    alert.getTenantId().value(),
                    alert.getVehicleId().value(),
                    alert.getAlertType().name(),
                    alert.getStatus().name(),
                    alert.getConfidenceScore().percentage(),
                    alert.getMessage(),
                    event.dispatchedAt()
            );

            eventPublisher.publishEvent(integrationEvent);
            log.debug("Published PredictiveAlertGeneratedIntegrationEvent for alert: {}", alert.getId());
        });
    }
}
