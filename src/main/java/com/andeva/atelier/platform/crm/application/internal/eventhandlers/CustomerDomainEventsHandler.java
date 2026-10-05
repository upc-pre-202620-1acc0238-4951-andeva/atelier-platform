package com.andeva.atelier.platform.crm.application.internal.eventhandlers;

import com.andeva.atelier.platform.crm.domain.model.events.CustomerContactUpdatedEvent;
import com.andeva.atelier.platform.crm.domain.model.events.CustomerRegisteredEvent;
import com.andeva.atelier.platform.crm.interfaces.events.CustomerCreatedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Application event listener orchestrating side effects on customer domain events.
 *
 * @author Adiel Sanchez Santin
 */
@Component
public class CustomerDomainEventsHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomerDomainEventsHandler.class);

    private final ApplicationEventPublisher eventPublisher;

    public CustomerDomainEventsHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @EventListener
    public void on(CustomerRegisteredEvent event) {
        log.info("Processing CustomerRegisteredEvent for customer ID: {}, tenant: {}, type: {}",
                event.customerId(), event.tenantId(), event.type());

        CustomerCreatedIntegrationEvent integrationEvent = new CustomerCreatedIntegrationEvent(
                event.customerId().value(),
                event.tenantId().value(),
                event.type().name(),
                event.displayName(),
                event.taxId().value(),
                null,
                null,
                event.occurredOn()
        );
        eventPublisher.publishEvent(integrationEvent);
    }

    @EventListener
    public void on(CustomerContactUpdatedEvent event) {
        log.info("Processing CustomerContactUpdatedEvent for customer ID: {}", event.customerId());
    }
}
